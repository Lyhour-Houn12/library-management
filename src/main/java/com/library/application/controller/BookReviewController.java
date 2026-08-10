package com.library.application.controller;

import com.library.application.domain.ReviewFilterType;
import com.library.application.payload.dto.BookReviewDTO;
import com.library.application.payload.request.BookReviewRequest;
import com.library.application.payload.request.UpdateReviewRequest;
import com.library.application.payload.response.PageResponse;
import com.library.application.service.BookReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/book-reviews")
@RequiredArgsConstructor
public class BookReviewController {
    private final BookReviewService bookReviewService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createBookReview(@Valid @RequestBody BookReviewRequest bookReviewRequest) {
        return new  ResponseEntity<>(bookReviewService.createBookReview(bookReviewRequest), HttpStatus.CREATED);
    }

    @PutMapping("/update/{reviewId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createBookReview(@PathVariable Long reviewId, @Valid @RequestBody UpdateReviewRequest bookReviewRequest) {
        return new  ResponseEntity<>(bookReviewService.updateBookReview(reviewId, bookReviewRequest), HttpStatus.OK);
    }

    @PatchMapping("/delete/{reviewId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> deleteBookReview(@PathVariable Long reviewId) {
        bookReviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getBookReview(@PathVariable Long reviewId) {
        return new  ResponseEntity<>(bookReviewService.getBookReview(reviewId), HttpStatus.OK);

    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<?> getReviewsBook(@PathVariable Long bookId,
                                            @RequestParam(defaultValue = "ALL") ReviewFilterType type,
                                            @RequestParam(required = false) Integer rating,
                                            @RequestParam(defaultValue = "0") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(bookReviewService.getReviewsByBookFilter(bookId, type, rating, page, size));
    }
    @PostMapping("/{reviewId}/helpful")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> markReviewAsHelpful(@PathVariable Long reviewId) {
        BookReviewDTO updatedReview = bookReviewService.markReviewAsHelpful(reviewId);
        return ResponseEntity.ok(updatedReview);
    }

    @GetMapping("/my-reviews")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getMyBookReview(@RequestParam(required = false, defaultValue = "0") Integer page, @RequestParam(required = false, defaultValue = "10") Integer size) {
        return new ResponseEntity<>(bookReviewService.myReviews(page, size), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<BookReviewDTO>> getReviewsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<BookReviewDTO> reviews = bookReviewService.getReviewsByUser(userId, page, size);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get rating statistics for a book
     * GET /api/reviews/book/{bookId}/statistics
     *
     * Returns:
     * - Average rating
     * - Total number of reviews
     * - Rating distribution (count of 1-star, 2-star, etc.)
     * - Number of verified reader reviews
     *
     * Example response:
     * {
     *   "bookId": 1,
     *   "bookTitle": "Effective Java",
     *   "averageRating": 4.5,
     *   "totalReviews": 120,
     *   "ratingDistribution": {
     *     "5": 80,
     *     "4": 30,
     *     "3": 8,
     *     "2": 2,
     *     "1": 0
     *   },
     *   "verifiedReaderReviews": 95
     * }
     */

    @GetMapping("/book/{bookId}/statistics")
    public ResponseEntity<?> getRatingStatistics(@PathVariable Long bookId) {
        return ResponseEntity.ok(bookReviewService.getStatistics(bookId));
    }

    @GetMapping("/can-review/{bookId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> canReviewBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(new CanReviewResponse(bookReviewService.canUserReviewBook(bookId)));
    }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReviewStatistics() {
        long totalReview = bookReviewService.getTotalReviewCount();
        return ResponseEntity.ok(new ReviewStatisticsResponse(totalReview));
    }


    // ==================== RESPONSE DTOs ====================

    /**
     * Response DTO for can-review endpoint
     */
    public static class CanReviewResponse {
        public boolean canReview;

        public CanReviewResponse(boolean canReview) {
            this.canReview = canReview;
        }
    }

    /**
     * Response DTO for review statistics endpoint
     */
    public static class ReviewStatisticsResponse {
        public long totalReviews;

        public ReviewStatisticsResponse(long totalReviews) {

            this.totalReviews = totalReviews;
        }
    }

}
