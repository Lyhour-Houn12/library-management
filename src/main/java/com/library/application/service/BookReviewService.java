package com.library.application.service;

import com.library.application.domain.ReviewFilterType;
import com.library.application.payload.dto.BookRatingStatisticsDTO;
import com.library.application.payload.dto.BookReviewDTO;
import com.library.application.payload.request.BookReviewRequest;
import com.library.application.payload.request.UpdateReviewRequest;
import com.library.application.payload.response.PageResponse;

public interface BookReviewService {

    BookReviewDTO createBookReview(BookReviewRequest bookReviewRequest);

    BookReviewDTO updateBookReview(Long reviewId, UpdateReviewRequest request);

    void deleteReview(Long reviewId);

    BookReviewDTO getBookReview(Long reviewId);

    PageResponse<BookReviewDTO> getReviewsByBookFilter(Long bookId, ReviewFilterType type, Integer rating, Integer page, Integer size);

    PageResponse<BookReviewDTO> myReviews(Integer page, Integer size);

    /**
     * Get all reviews by a specific user
     */
    PageResponse<BookReviewDTO> getReviewsByUser(Long userId, Integer page, Integer size);

    BookRatingStatisticsDTO getStatistics(Long bookId);

    /**
     * Mark a review as helpful
     */
    BookReviewDTO markReviewAsHelpful(Long reviewId);

    /**
     * Check if current user can review a book
     */
    boolean canUserReviewBook(Long bookId);

    /**
     * Check if a specific user can review a book
     */
    boolean canUserReviewBook(Long userId, Long bookId);

    /**
     * Get total count of all active reviews (Admin only)
     */
    long getTotalReviewCount();
}
