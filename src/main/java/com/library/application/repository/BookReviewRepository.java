package com.library.application.repository;

import com.library.application.entity.BookReview;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    Page<BookReview> findByBookIdAndRatingAndIsActiveTrue(Long bookId, Integer rating, Pageable pageable);

    Page<BookReview> findByBookIdAndIsActiveTrue(Long bookId, Pageable pageable);


    @Query("""
        SELECT br FROM BookReview  br WHERE br.book.id = :bookId AND br.isActive = true ORDER BY br.helpfulCount DESC
        """)
    Page<BookReview> findByBookIdAndHelpfulCountOrderByHelpfulCountDesc(@Param("bookId") Long bookId, Pageable pageable);

    Page<BookReview> findByBookIdAndIsVerifiedReaderTrue(Long bookId, Pageable pageable);

    Page<BookReview> findByUserIdAndIsActiveTrue(Long userId, Pageable pageable);

    @Query("""
        SELECT AVG(br.rating) FROM BookReview br WHERE br.book.id = :bookId AND br.isActive = TRUE
        """)
    Double getAverageRatingByBookId(@Param("bookId") Long bookId);

    @Query("""
        SELECT COUNT(br) FROM BookReview  br WHERE br.book.id = :bookId AND br.isActive = TRUE
        """)
    Long getTotalReviewByBookId(@Param("bookId") Long bookId);

    @Query("""
        SELECT br.rating, COUNT(br) FROM BookReview br WHERE br.book.id = :bookId AND br.isActive = TRUE GROUP BY br.rating
        """)
    List<Object[]> countReviewByRatingForBook(@Param("bookId") Long bookId);

    Page<BookReview> findByBookIdAndIsVerifiedReaderTrueAndIsActiveTrue(Long bookId, Pageable pageable);

    long countByIsActiveTrue();
}
