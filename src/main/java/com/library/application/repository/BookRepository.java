package com.library.application.repository;

import com.library.application.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Boolean existsByIsbn(String isbn);

    Optional<Book> findByIsbn(String isbn);


    @Query("""
SELECT b
FROM Book b
WHERE b.active = true
AND (
    :searchTerm IS NULL
    OR :searchTerm = ''
    OR LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    OR LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
)
AND (
    :genreId IS NULL
    OR b.genre.id = :genreId
)
AND (
    :availableOnly = false
    OR b.availableCopies > 0
)
""")
    Page<Book> searchBookWithFilter(
            @Param("searchTerm") String searchTerm,
            @Param("genreId") Long genreId,
            @Param("availableOnly") Boolean availableOnly,
            Pageable pageable
    );

    @Query("SELECT COUNT(b) FROM Book b WHERE b.availableCopies > 0 AND b.active = true")
    Long countByActiveTrue();

    @Query("SELECT COUNT(b) FROM Book b WHERE b.availableCopies > 0 AND b.active = false")
    Long countByActiveFalse();

    @Query("SELECT SUM(b.availableCopies) FROM Book b WHERE b.availableCopies > 0 AND b.active = true")
    Long countAvailableBooks();

    @Query("SELECT sum(b.availableCopies) FROM Book b WHERE b.availableCopies > 0 AND b.active = false")
    Long countUnAvailableBooks();
}
