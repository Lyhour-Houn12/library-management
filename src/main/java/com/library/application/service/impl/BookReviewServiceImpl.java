package com.library.application.service.impl;

import com.library.application.domain.BookLoanStatus;
import com.library.application.domain.ReviewFilterType;
import com.library.application.entity.Book;
import com.library.application.entity.BookLoan;
import com.library.application.entity.BookReview;
import com.library.application.entity.User;
import com.library.application.exception.BookException;
import com.library.application.exception.BookReviewException;
import com.library.application.mapper.BookReviewMapper;
import com.library.application.payload.dto.BookRatingStatisticsDTO;
import com.library.application.payload.dto.BookReviewDTO;
import com.library.application.payload.request.BookReviewRequest;
import com.library.application.payload.request.UpdateReviewRequest;
import com.library.application.payload.response.PageResponse;
import com.library.application.repository.BookLoanRepository;
import com.library.application.repository.BookRepository;
import com.library.application.repository.BookReviewRepository;
import com.library.application.service.BookReviewService;
import com.library.application.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Slf4j
public class BookReviewServiceImpl implements BookReviewService {
    private final BookReviewRepository bookReviewRepository;
    private final UserService userService;
    private final BookReviewMapper  bookReviewMapper;
    private final BookRepository bookRepository;
    private final BookLoanRepository bookLoanRepository;


    @Override
    public BookReviewDTO createBookReview(BookReviewRequest bookReviewRequest) {
        User user = userService.getCurrentUser();

        Book book = bookRepository.findById(bookReviewRequest.getBookId())
                .orElseThrow(() -> new BookException(String.format("Book with id: %s not found", bookReviewRequest.getBookId())));


        // check if user has already reviewed this book
        if(bookReviewRepository.existsByUserIdAndBookId(user.getId(), book.getId())){
            throw new BookReviewException("You have already reviewed this book. You can edit your previous review instead.");
        }

        // IMPORTANT: check if user had read the book (complete a loan)
        boolean hasReadBook = hasUserReadBook(user.getId(), bookReviewRequest.getBookId());
        if(!hasReadBook){
            throw new BookReviewException("You can only review the book you read. Please checkout and return first before reviewing the book");
        }

        BookReview  bookReview = new BookReview();
        bookReview.setBook(book);
        bookReview.setUser(user);
        bookReview.setRating(bookReviewRequest.getRating());
        bookReview.setTitle(bookReviewRequest.getTitle());
        bookReview.setReviewText(bookReviewRequest.getReviewText());
        bookReview.setIsVerifiedReader(true);
        bookReview.setIsActive(true);
        bookReview.setHelpfulCount(0);

        bookReview = bookReviewRepository.save(bookReview);
        return bookReviewMapper.toDTO(bookReview);
    }



    @Override
    public BookReviewDTO updateBookReview(Long reviewId, UpdateReviewRequest request) {
        User user = userService.getCurrentUser();

        BookReview bookReview = bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new BookException(String.format("Book with id: %s not found", reviewId)));

        if(!bookReview.getUser().getId().equals(user.getId())){
            throw new BookReviewException("You can edit your own book");
        }

        bookReview.setRating(request.getRating());
        bookReview.setTitle(request.getTitle());
        bookReview.setReviewText(request.getReviewText());
        bookReview = bookReviewRepository.save(bookReview);

        return bookReviewMapper.toDTO(bookReview);
    }

    @Override
    public void deleteReview(Long reviewId) {
        User user = userService.getCurrentUser();

        BookReview bookReview = bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new BookException(String.format("Book with id: %s not found", reviewId)));

        if(!bookReview.getUser().getId().equals(user.getId())){
            throw new BookReviewException("You can edit your own book");
        }

        bookReview.setIsActive(false);
        bookReviewRepository.save(bookReview);
    }

    @Override
    public BookReviewDTO getBookReview(Long reviewId) {
        BookReview bookReview = bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new BookException(String.format("Book with id: %s not found", reviewId)));

        return bookReviewMapper.toDTO(bookReview);
    }

    @Override
    public PageResponse<BookReviewDTO> getReviewsByBookFilter(Long bookId, ReviewFilterType type, Integer rating, Integer page, Integer size) {
        Page<BookReview> reviewPage;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        switch (type){
            case BY_RATING:
                if(rating == null){
                    throw new IllegalArgumentException("Rating is required when filter type is BY_RATING");
                }
                if(rating < 1 || rating > 5){
                    throw new IllegalArgumentException("Rating must be between 1 and 5");
                }
                reviewPage = bookReviewRepository.findByBookIdAndRatingAndIsActiveTrue(bookId, rating, pageable);
                break;
            case VERIFIED_ONLY:
                reviewPage = bookReviewRepository.findByBookIdAndIsVerifiedReaderTrue(bookId, pageable);
                break;
            case TOP_HELPFUL:
                reviewPage = bookReviewRepository.findByBookIdAndHelpfulCountOrderByHelpfulCountDesc(bookId, pageable);
                break;
            default:
                    reviewPage = bookReviewRepository.findByBookIdAndIsActiveTrue(bookId, pageable);
                    break;
        }
        return PageResponse.from(
                reviewPage.map(bookReviewMapper::toDTO)
        );
    }

    @Override
    public PageResponse<BookReviewDTO> myReviews(Integer page, Integer size) {
        User user = userService.getCurrentUser();
        return getReviewsByUser(user.getId(), page, size);
    }

    @Override
    public PageResponse<BookReviewDTO> getReviewsByUser(Long userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BookReview> reviewPage  = bookReviewRepository.findByUserIdAndIsActiveTrue(userId, pageable);
        return PageResponse.from(reviewPage.map(bookReviewMapper::toDTO));
    }

    @Override
    public BookRatingStatisticsDTO getStatistics(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookException(String.format("Book with id: %s not found", bookId)));
        // get average rating
        Double averageRating = bookReviewRepository.getAverageRatingByBookId(bookId);

        // get total reviews
        Long totalReviews = bookReviewRepository.getTotalReviewByBookId(bookId);

        // get rating distribution
        List<Object[]> ratingData = bookReviewRepository.countReviewByRatingForBook(bookId);
        Map<Integer, Long> ratingDistribution = new HashMap<>();

        // Initialize all ratings with 0
        for(int i =  0; i < 5; i++){
            ratingDistribution.put(i, 0L);
        }

        // Fill in actual count
        for(Object[] row :  ratingData){
            Integer rating = (Integer)row[0];
            Long count = (Long)row[1];
            ratingDistribution.put(rating, count);
        }

        Pageable pageable = PageRequest.of(0,1);
        Long verifiedReaderReviews = bookReviewRepository
                .findByBookIdAndIsVerifiedReaderTrueAndIsActiveTrue(bookId, pageable)
                .getTotalElements();

        return BookRatingStatisticsDTO.builder()
                .bookId(bookId)
                .bookTitle(book.getTitle())
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews)
                .ratingDistribution(ratingDistribution)
                .verifiedReaderReviews(verifiedReaderReviews)
                .build();
    }

    @Override
    public BookReviewDTO markReviewAsHelpful(Long reviewId) {
        BookReview bookReview = bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new BookException(String.format("Book with id: %s not found", reviewId)));
        bookReview.setHelpfulCount(bookReview.getHelpfulCount() + 1);
        BookReview updateReview = bookReviewRepository.save(bookReview);
        return bookReviewMapper.toDTO(updateReview);
    }

    @Override
    public boolean canUserReviewBook(Long bookId) {
        User user = userService.getCurrentUser();
        return canUserReviewBook(user.getId(), bookId);
    }

    @Override
    public boolean canUserReviewBook(Long userId, Long bookId) {
        // User can review if they have not already reviewed AND have read the book
        boolean alreadyReviewed = bookReviewRepository.existsByUserIdAndBookId(bookId, userId);
        boolean hadReadBook =hasUserReadBook(userId, bookId);
        return !alreadyReviewed && hadReadBook;
    }

    @Override
    public long getTotalReviewCount() {
        return bookReviewRepository.countByIsActiveTrue();
    }

    private boolean hasUserReadBook(Long userId, Long bookId) {
        Pageable pageable = PageRequest.of(0, 1);
        Page<BookLoan> bookLoans = bookLoanRepository.findByBookId(bookId, pageable);

        return bookLoans.stream()
                .anyMatch(loan -> loan.getUser().getId().equals(userId) && loan.getStatus() == BookLoanStatus.RETURNED);
    }
}
