package com.library.application.service.impl;

import com.library.application.domain.BookLoanStatus;
import com.library.application.domain.BookLoanType;
import com.library.application.entity.Book;
import com.library.application.entity.BookLoan;
import com.library.application.entity.User;
import com.library.application.exception.BookException;
import com.library.application.exception.BookLoanException;
import com.library.application.mapper.BookLoanMapper;
import com.library.application.payload.dto.BookLoanDTO;
import com.library.application.payload.dto.SubscriptionDTO;
import com.library.application.payload.request.BookLoanSearchRequest;
import com.library.application.payload.request.CheckInRequest;
import com.library.application.payload.request.CheckoutRequest;
import com.library.application.payload.request.RenewalRequest;
import com.library.application.payload.response.PageResponse;
import com.library.application.repository.BookLoanRepository;
import com.library.application.repository.BookRepository;
import com.library.application.repository.UserRepository;
import com.library.application.service.BookLoanService;
import com.library.application.service.SubscriptionService;
import com.library.application.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

        // update book copy
        if(condition != BookLoanStatus.LOST){
            Book book =  bookLoan.getBook();
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.save(book);
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
            throw new BookLoanException("Book loan cannot be renewed.");
        }
        bookLoan.setDueDate(bookLoan.getDueDate().plusDays(request.getExtensionDays()));
        bookLoan.setRenewalCount(bookLoan.getRenewalCount() + 1);
        bookLoan.setNotes(request.getNotes());

        BookLoan savedBookLoan = bookLoanRepository.save(bookLoan);
        return bookLoanMapper.toDto(savedBookLoan);
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


    // @TOD0 WHEN IMPLEMENTING FINE
    @Override
    public Long updateOverdueBookLoans() {
        Pageable pageable = PageRequest.of(0, 1000);
        Page<BookLoan> overduePage =  bookLoanRepository.findOverdueBookLoans(LocalDate.now(), pageable);

        Long updateCount = 0L;
        for(BookLoan bookLoan : overduePage.getContent()){
            if (bookLoan.getStatus() == BookLoanStatus.CHECKOUT) {
                bookLoan.setStatus(BookLoanStatus.OVERDUE);
                bookLoan.setIsOverdue(true);

                bookLoanRepository.save(bookLoan);
                updateCount++;
            }
        }
        return updateCount;
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


}
