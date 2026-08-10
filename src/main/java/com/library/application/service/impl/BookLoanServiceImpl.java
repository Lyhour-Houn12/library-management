package com.library.application.service.impl;

import com.library.application.domain.BookLoanStatus;
import com.library.application.domain.BookLoanType;
import com.library.application.domain.FineStatus;
import com.library.application.domain.FineType;
import com.library.application.entity.Book;
import com.library.application.entity.BookLoan;
import com.library.application.entity.Fine;
import com.library.application.entity.User;
import com.library.application.exception.BookException;
import com.library.application.exception.BookLoanException;
import com.library.application.exception.FineException;
import com.library.application.mapper.BookLoanMapper;
import com.library.application.payload.CheckoutStatistics;
import com.library.application.payload.dto.BookLoanDTO;
import com.library.application.payload.dto.SubscriptionDTO;
import com.library.application.payload.request.*;
import com.library.application.payload.response.PageResponse;
import com.library.application.repository.*;
import com.library.application.service.*;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookLoanServiceImpl implements BookLoanService {
    private final BookLoanRepository bookLoanRepository;
    private final SubscriptionService  subscriptionService;
    private final BookRepository bookRepository;
    private final UserService userService;
    private final BookLoanMapper  bookLoanMapper;
    private final UserRepository userRepository;
    private final FineCalculateService fineCalculateService;
    private final FineRepository fineRepository;

    @Setter
    private ReservationService reservationService;  // Lazy injection to avoid circular dependency

    private static final int DEFAULT_CHECKOUT_DAYS = 14;


    @Override
    public BookLoanDTO checkoutBookLoan(CheckoutRequest request) {
        User user = userService.getCurrentUser();
        return checkoutBookForUser(user.getId(), request);
    }

    @Override
    public BookLoanDTO checkoutBookForUser(Long userId, CheckoutRequest request) {
        // 1. Find user
        User user = userService.getUserById(userId);

        // 2. Validate User has active subscription
        SubscriptionDTO subscription;
        try{
            subscription = subscriptionService.getUserActiveSubscription(userId);
        }catch (Exception e){
            throw new BookLoanException("No active subscription found. Please subscribe before checkout books.");
        }

        // 3. Validate book exists and is available
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BookException("Book not found with given id: " +  request.getBookId()));

        if(!book.getActive()){
            throw new BookException("Book is not active and cannot be checked out.");
        }

        if(book.getAvailableCopies() <= 0){
            throw new BookException("Book has no available copies, so it cannot be checked out.");
        }

        // 4. Check if user has already checked out this book
        if(bookLoanRepository.hasActiveCheckout(userId, book.getId())){
            throw new BookException("Book has already checked out");
        }


        // 5. Check user's active checkout limit (enforced by subscription)
        Long activeCheckouts = bookLoanRepository.countActiveBookLoanByUserId(userId);
        Integer maxBooksAllowed = subscription.getMaxBooksAllowed();
        if(activeCheckouts >= maxBooksAllowed){
            throw new BookLoanException(
                    "You have reached your subscription limited of " +  maxBooksAllowed + " active checkouts." +
                    "Your current plan: " + subscription.getPlanName() + ". " +
                            "Please return books or upgrade your subscription for more checkout.");
        }

        // 6. Check for overdue books
        Long overdueCount = bookLoanRepository.countOverdueBookLoanByUserId(userId);
        if (overdueCount > 0) {
            throw new BookLoanException(
                    "User has " + overdueCount + " overdue book(s). Cannot checkout until books are returned.");
        }

        BookLoan bookLoan = new BookLoan();
        bookLoan.setUser(user);
        bookLoan.setBook(book);
        bookLoan.setType(BookLoanType.CHECKOUT);
        bookLoan.setStatus(BookLoanStatus.CHECKOUT);
        bookLoan.setCheckoutDate(LocalDate.now());

        // Use subscription's maxDaysPerBook if no specific checkout days requested
        int checkoutDays = request.getCheckoutDate() != null
                ? Math.min(request.getCheckoutDate(), subscription.getMaxDaysPerBook())
                : subscription.getMaxDaysPerBook();
        bookLoan.setDueDate(LocalDate.now().plusDays(checkoutDays));

        bookLoan.setRenewalCount(0);
        bookLoan.setMaxRenewal(2);
        bookLoan.setNotes(request.getNotes());
        bookLoan.setIsOverdue(false);
        bookLoan.setOverdueDays(0);


        // update available copy book
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        BookLoan savedBookLoan = bookLoanRepository.save(bookLoan);

        return bookLoanMapper.toDto(savedBookLoan);
    }

    @Override
    public BookLoanDTO checkInBookLoan(CheckInRequest request) {
        BookLoan bookLoan = bookLoanRepository.findById(request.getBookLoanId())
                .orElseThrow(() -> new BookLoanException("Book loan not found with given id: " +  request.getBookLoanId()));

        if(!bookLoan.isActive()){
            throw new BookLoanException("Book loan has already been returned.");
        }
        bookLoan.setReturnDate(LocalDate.now());

        BookLoanStatus condition = request.getCondition();
        if(condition == null){
            condition = BookLoanStatus.RETURNED;
        }

        if(condition != BookLoanStatus.RETURNED
        && condition != BookLoanStatus.DAMAGED
        && condition != BookLoanStatus.LOST){
            throw new BookLoanException("Invalid return condition. Must be RETURNED, LOST, or DAMAGED");
        }
        bookLoan.setStatus(condition);

        bookLoan.setOverdueDays(0);

        bookLoan.setIsOverdue(false);

        // update book available copy (only if not lost)
        if(condition != BookLoanStatus.LOST){
            Book book =  bookLoan.getBook();
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.save(book);

            // process next reservation if book becomes available
            processNextReservation(book.getId());
        }
        bookLoan.setNotes("Returned book by user.");

        BookLoan savedBookLoan = bookLoanRepository.save(bookLoan);
        return bookLoanMapper.toDto(savedBookLoan);
    }

    @Override
    public BookLoanDTO renewalBookLoan(RenewalRequest request) {
        BookLoan bookLoan = bookLoanRepository.findById(request.getBookLoanId())
                .orElseThrow(() -> new BookLoanException("Book loan not found with given id: " +  request.getBookLoanId()));

        if(!bookLoan.canRenew()){
            if(bookLoan.getIsOverdue()){
                throw new BookLoanException("Cannot renew overdue books. Please return it back.");
            }
            if(bookLoan.getRenewalCount() > bookLoan.getMaxRenewal()){
                throw new BookLoanException("Maximum renewal limit reached (" + bookLoan.getMaxRenewal() + ")");
            }
            throw new BookLoanException("Book loan cannot be renewed.");
        }

        // 3. Update due date
        int extensionDays = request.getExtensionDays() != null
                ? request.getExtensionDays()
                : DEFAULT_CHECKOUT_DAYS;
        bookLoan.setDueDate(bookLoan.getDueDate().plusDays(extensionDays));
        bookLoan.setRenewalCount(bookLoan.getRenewalCount() + 1);
        bookLoan.setNotes(request.getNotes());

        BookLoan savedBookLoan = bookLoanRepository.save(bookLoan);
        return bookLoanMapper.toDto(savedBookLoan);
    }

    @Override
    public BookLoanDTO getBookLoanById(Long bookLoanId) {
        BookLoan bookLoan = bookLoanRepository.findById(bookLoanId)
                .orElseThrow(() -> new BookLoanException("Book loan not found with given id: " + bookLoanId));
        return bookLoanMapper.toDto(bookLoan);
    }

    @Override
    public PageResponse<BookLoanDTO> getBookLoans(BookLoanSearchRequest request) {
        Pageable pageable = createPageRequest(
                request.getPage(),
                request.getSize(),
                request.getSortBy(),
                request.getSortDirection()
        );

        Page<BookLoan> bookLoanPage;

        // apply filtering logic dynamically
        if(Boolean.TRUE.equals(request.getOverDueOnly())) {
            bookLoanPage = bookLoanRepository.findOverdueBookLoans(LocalDate.now(), pageable);
        }
        else if(request.getUserId() != null){
            bookLoanPage = bookLoanRepository.findByUserId(request.getUserId(), pageable);
        }
        else if(request.getBookId() != null){
            bookLoanPage = bookLoanRepository.findByBookId(request.getBookId(), pageable);
        }
        else if(request.getStatus() != null){
            bookLoanPage = bookLoanRepository.findByStatus(request.getStatus(), pageable);
        }
        else if(request.getStartDate() != null && request.getEndDate() != null){
            bookLoanPage = bookLoanRepository.findBookLoansByDataRange(request.getStartDate(), request.getEndDate(), pageable);
        }else{
            bookLoanPage = bookLoanRepository.findAll(pageable);
        }
        return convertToPageResponse(bookLoanPage);
    }

    public PageResponse<BookLoanDTO> getMyBookLoans(BookLoanStatus status,
                                                    Integer page, Integer size){
        User currentUser = userService.getCurrentUser();
        return getUserBookLoans(currentUser.getId(), status, page, size);
    }

    @Override
    public PageResponse<BookLoanDTO> getUserBookLoans(Long userId,
                                                    BookLoanStatus status,
                                                    Integer page, Integer size) {
        User currentUser = userService.getCurrentUser();
        currentUser = userRepository.findById(userId).orElseThrow(() -> new BookLoanException("User not found with given id: " + userId));
        Page<BookLoan> bookLoanPage;
        if(status != null){
            // Return only checkout, sorted by duedate
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "dueDate"));
            bookLoanPage = bookLoanRepository.findByStatusAndUser(status, currentUser, pageable);
        }else{
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            bookLoanPage = bookLoanRepository.findByUserId(userId ,pageable);
        }
        return convertToPageResponse(bookLoanPage);
    }



    @Override
    public Long updateOverdueBookLoans() {
        Pageable pageable = PageRequest.of(0, 1000);
        Page<BookLoan> overduePage =  bookLoanRepository.findOverdueBookLoans(LocalDate.now(), pageable);

        Long updateCount = 0L;
        for(BookLoan bookLoan : overduePage.getContent()){
            if (bookLoan.getStatus() == BookLoanStatus.CHECKOUT || bookLoan.getStatus() == BookLoanStatus.OVERDUE) {
                bookLoan.setStatus(BookLoanStatus.OVERDUE);
                bookLoan.setIsOverdue(true);

                // calculate overdue day
                int overdueDays = fineCalculateService.calculateOverdueDays(bookLoan.getDueDate(), LocalDate.now());
                bookLoan.setOverdueDays(overdueDays);
                bookLoanRepository.save(bookLoan);
                // calculate fine
                Double amount = fineCalculateService.calculateOverdueFine(bookLoan)
                        .doubleValue();
                Fine fine = fineRepository.findByBookLoanAndFineType(bookLoan, FineType.OVERDUE)
                        .orElse(null);

                if (fine == null) {

                    fine = Fine.builder()
                            .bookLoan(bookLoan)
                            .user(bookLoan.getUser())
                            .fineType(FineType.OVERDUE)
                            .amount(amount)
                            .amountPaid(0D)
                            .status(FineStatus.PENDING)
                            .reason("Book loan is overdue")
                            .build();

                } else if (fine.getStatus() != FineStatus.PAID
                        && fine.getStatus() != FineStatus.WAIVED) {
                    fine.setAmount(amount);
                }

                fineRepository.save(fine);

                updateCount++;
            }
        }
        return updateCount;
    }

    @Override
    public BookLoanDTO updateBookLoan(Long bookLoanId, UpdateBookLoanRequest updateRequest) {
        BookLoan bookLoan = bookLoanRepository.findById(bookLoanId)
                .orElseThrow(() -> new BookLoanException("Book Loan not found with given id: " + bookLoanId));
        // 2. Update fields if provided (null values are ignored)
        if (updateRequest.getStatus() != null) {
            bookLoan.setStatus(updateRequest.getStatus());
        }

        if (updateRequest.getDueDate() != null) {
            bookLoan.setDueDate(updateRequest.getDueDate());
        }

        if (updateRequest.getReturnDate() != null) {
            bookLoan.setReturnDate(updateRequest.getReturnDate());
        }

        if (updateRequest.getMaxRenewals() != null) {
            bookLoan.setMaxRenewal(updateRequest.getMaxRenewals());
        }



        if (updateRequest.getNotes() != null) {
            String existingNotes = bookLoan.getNotes() != null ? bookLoan.getNotes() + "\n" : "";
            bookLoan.setNotes(existingNotes + "Admin update: " + updateRequest.getNotes());
        }

        // 3. Save and return
        BookLoan savedBookLoan = bookLoanRepository.save(bookLoan);
        return bookLoanMapper.toDto(savedBookLoan);
    }

    @Override
    public CheckoutStatistics getCheckoutStatistics() {
        long totalCheckouts = bookLoanRepository.count();

        long totalActiveCheckout = bookLoanRepository.findAll()
                .stream()
                .filter(BookLoan::isActive)
                .count();
        long totalOverdueCheckout = bookLoanRepository
                .findOverdueBookLoans(LocalDate.now(), PageRequest.of(0, Integer.MAX_VALUE))
                .getTotalElements();
        long totalReturns = bookLoanRepository.findByStatus(BookLoanStatus.RETURNED, PageRequest.of(0, Integer.MAX_VALUE))
                .getTotalElements();
        return new CheckoutStatistics(
                totalCheckouts,
                totalActiveCheckout,
                totalOverdueCheckout,
                totalReturns,
                null,
0
        );
    }

    private Pageable createPageRequest(Integer page, Integer size, String sortBy, String sortDirection) {
        size = Math.min(size, 100);
        size = Math.max(1, size);

        Sort sort = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        return PageRequest.of(page, size, sort);
    }

    private PageResponse<BookLoanDTO> convertToPageResponse(Page<BookLoan> bookLoanPage){
        List<BookLoanDTO> bookLoans = bookLoanPage.getContent()
                .stream()
                .map(bookLoanMapper::toDto)
                .toList();
        return new PageResponse<>(
                bookLoans,
                bookLoanPage.getNumber(),
                bookLoanPage.getSize(),
                bookLoanPage.getTotalElements(),
                bookLoanPage.getTotalPages(),
                bookLoanPage.isFirst(),
                bookLoanPage.isLast(),
                bookLoanPage.isEmpty()
        );
    }

    /**
     * Process next reservation when book becomes available
     */
    private void processNextReservation(Long bookId){
        if(reservationService != null){
            try{
                reservationService.processNextReservation(bookId);
            }catch (Exception e){
                // Log but don't fail the check-in process
                System.err.println("Failed to process reservation for book " + bookId + ": " + e.getMessage());
            }
        }
    }


}
