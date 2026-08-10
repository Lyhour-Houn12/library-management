package com.library.application.repository;

import com.library.application.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Boolean existsByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);

    void deleteByUserIdAndBookId(Long id, Long bookId);


    Page<Wishlist> findByUserId(Long userId, Pageable pageable);

    Wishlist findByUserIdAndBookId(Long id, Long bookId);


    @Query("""
        SELECT COUNT(w) FROM Wishlist w WHERE w.user.id = :userId
        """)
    Long countWishlistByUserId(@Param("userId") Long userId);


    @Query("""
        SELECT COUNT(w) FROM Wishlist w WHERE w.book.id = :bookId
        """)
    Long countByBookId(@Param("bookId") Long bookId);
}
