package com.library.application.service.impl;

import com.library.application.domain.BookLoanStatus;
import com.library.application.domain.ReservationStatus;
import com.library.application.domain.UserRole;
import com.library.application.entity.Book;
import com.library.application.entity.Reservation;
import com.library.application.entity.User;
import com.library.application.exception.BookException;
import com.library.application.exception.ReservationException;
import com.library.application.mapper.ReservationMapper;
import com.library.application.payload.dto.ReservationDTO;
import com.library.application.payload.request.CheckoutRequest;
import com.library.application.payload.request.ReservationRequest;
import com.library.application.payload.request.ReservationSearchRequest;
import com.library.application.payload.response.PageResponse;
import com.library.application.repository.BookLoanRepository;
import com.library.application.repository.BookRepository;
import com.library.application.repository.ReservationRepository;
import com.library.application.repository.UserRepository;
import com.library.application.service.BookLoanService;
import com.library.application.service.ReservationService;
import com.library.application.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {
    private static final Integer MAX_ACTIVE_RESERVATIONS = 5; // PER USER
    private static final Integer HOLD_PERIOD_HOURS  = 48;


    private final ReservationRepository reservationRepository;
    private final UserService userService;
    private final BookRepository bookRepository;
    private final BookLoanRepository bookLoanRepository;
    private final UserRepository userRepository;
    private final ReservationMapper  reservationMapper;
    private final BookLoanService bookLoanService;


    @Override
    @Transactional
    public ReservationDTO createReservation(ReservationRequest request) {
        User user = userService.getCurrentUser();
        return createReservationForUser(user.getId(), request);
    }

    @Override
    @Transactional
    public ReservationDTO createReservationForUser(Long userId, ReservationRequest request) {
        boolean alreadyHasLoan = bookLoanRepository.existsByUserIdAndBookIdAndStatus(userId, request.getBookId(), BookLoanStatus.CHECKOUT);
        if(alreadyHasLoan) {
            throw new BookException("You have already loan on this book.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BookException("User not found with ID: " + userId));


        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BookException("Book not found with ID: " + request.getBookId()));

        if(!book.getActive()){
            throw new BookException("Book is not active.");
        }

        // check if user already has active reservation for this book
        if(reservationRepository.hasActiveReservation(userId, book.getId())){
            throw new ReservationException("You have already reserved for this book.");
        }

        // check if book available copy; users don't need to do request reservation
        if(book.getAvailableCopies() > 0){
            throw new BookException("Book is currently available; please check it out directly instead of reserving.");
        }

        // Check user's active reservation limit
        Long activeReservations = reservationRepository.countActiveReservationByUser(userId);
        if(activeReservations >= MAX_ACTIVE_RESERVATIONS){
            throw new ReservationException("You have reached the maximum number of reservations." + MAX_ACTIVE_RESERVATIONS);
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setNotificationSent(false);
        reservation.setNotes(request.getNotes());

        // calculate queue position
        long queueCount = reservationRepository.countPendingReservationByBook(book.getId());
        reservation.setQueuePosition((int) queueCount + 1);

        reservation = reservationRepository.save(reservation);

        return reservationMapper.toDTO(reservation);
    }

    @Override
    @Transactional
    public ReservationDTO cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException("Reservation not found with ID: " + reservationId));

        User user = userService.getCurrentUser();
        if(!reservation.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ROLE_USER){
            throw new ReservationException("User is not allowed to cancel reservation.");
        }

        if(!reservation.canBeCancelled()){
            throw new ReservationException("Reservation cannot be cancelled (current status: " + reservation.getStatus() + ")");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        Reservation savedReservation = reservationRepository.save(reservation);

        // Update queue positions for remaining reservations
        updateQueuePosition(reservation.getBook().getId());

        log.info("Reservation {} cancelled by user {}", reservationId, user.getId());
        return reservationMapper.toDTO(savedReservation);
    }

    @Override
    public ReservationDTO fulfilledReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException("Reservation not found with ID: " + reservationId));

        if(reservation.getBook().getAvailableCopies() <= 0){
            throw new ReservationException("Reservation is not available for picking up (current status: " + reservation.getStatus() + ")");
        }

        reservation.setStatus(ReservationStatus.FULFILLED);
        reservation.setFulfilledAt(LocalDateTime.now());
        Reservation savedReservation = reservationRepository.save(reservation);

        log.info("Reservation {} fulfilled", reservationId);
        CheckoutRequest checkoutRequest = new  CheckoutRequest();
        checkoutRequest.setBookId(reservation.getBook().getId());
        checkoutRequest.setNotes("Assign Booked by Admin");

        bookLoanService.checkoutBookForUser(reservation.getUser().getId(), checkoutRequest);
        return reservationMapper.toDTO(savedReservation);
    }

    @Override
    public ReservationDTO getReservationById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException("Reservation not found with ID: " + reservationId));
        return reservationMapper.toDTO(reservation);
    }

    @Override
    @Transactional
    public PageResponse<ReservationDTO> searchReservations(ReservationSearchRequest searchRequest) {
        Sort sort = searchRequest.getSortDirection().equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(Sort.Direction.ASC, searchRequest.getSortBy())
                : Sort.by(Sort.Direction.DESC,  searchRequest.getSortBy());
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);
        Page<Reservation> reservations = reservationRepository.searchReservationsWithFilters(
                searchRequest.getUserId(),
                searchRequest.getBookId(),
                searchRequest.getStatus(),
                searchRequest.getActiveOnly(),
                pageable
        );

        return PageResponse.from(reservations.map(reservationMapper::toDTO));
    }

    @Override
    @Transactional
    public PageResponse<ReservationDTO> getMyReservations(ReservationSearchRequest searchRequest) {
        ReservationSearchRequest reservationSearchRequest = new ReservationSearchRequest();
        User user = userService.getCurrentUser();
        reservationSearchRequest.setUserId(user.getId());
        return searchReservations(reservationSearchRequest);
    }

    @Override
    @Transactional
    public Integer getQueuePosition(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException("Reservation not found with ID: " + reservationId));
        if(reservation.getStatus() != ReservationStatus.PENDING){
            return 0;
        }
        return reservation.getQueuePosition() != null ? reservation.getQueuePosition() : 0;
    }


    // @TODO IMPLEMENTING WITH SENDING EMAIL
    @Override
    public void processNextReservation(Long bookId) {
        log.info("Processing next reservation for book id {}", bookId);

        // get next pending reservation
        var nextReservationOpt = reservationRepository.findNextPendingReservation(bookId);

        if(nextReservationOpt.isEmpty()){
            throw new ReservationException("No pending reservation found for book id " + bookId);
        }
        Reservation reservation = nextReservationOpt.get();
        reservation.setStatus(ReservationStatus.AVAILABLE);
        reservation.setAvailableAt(LocalDateTime.now());
        reservation.setAvailableUntil(LocalDateTime.now().plusHours(HOLD_PERIOD_HOURS));

        reservationRepository.save(reservation);

        updateQueuePosition(reservation.getBook().getId());

        log.info("Reservation {} marked as available for user {}", reservation.getId(), reservation.getUser().getId());
    }

    @Override
    @Transactional
    public Integer expiredOldReservation() {
        log.info("Starting expired old reservation");
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(LocalDateTime.now());

        for(Reservation reservation : expiredReservations){
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservation.setCancelledAt(LocalDateTime.now());
            reservationRepository.save(reservation);


            // Process next reservation for this book
            processNextReservation(reservation.getBook().getId());
        }

        log.info("Expired {} reservation(s)", expiredReservations.size());
        return expiredReservations.size();
    }

    @Override
    public void updateQueuePosition(Long bookId) {
        List<Reservation> pendingReservations = reservationRepository.findPendingReservationByBook(bookId);

        int position = 1;
        for(Reservation reservation : pendingReservations){
            reservation.setQueuePosition(position++);
            reservationRepository.save(reservation);
        }
        log.info("Updated queue positions for {} reservation(s) of book ID: {}", pendingReservations.size(), bookId);
    }
}
