package com.library.application.repository;

import com.library.application.domain.BookLoanStatus;
import com.library.application.entity.BookLoan;
import com.library.application.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

public interface BookLoanRepository extends JpaRepository<BookLoan, Long> {

    @Query("""
        SELECT CASE WHEN COUNT(bl) > 0 THEN TRUE ELSE FALSE END FROM BookLoan bl
        WHERE bl.user.id = :userId AND bl.book.id = :bookId
        AND (bl.status = 'CHECKOUT' OR bl.status = 'OVERDUE')\s
       \s""")
    Boolean hasActiveCheckout(@Param("userId") Long userId, @Param("bookId")  Long bookId);

    @Query("""
        SELECT COUNT(bl) FROM BookLoan bl
        WHERE bl.user.id = :userId AND
        (bl.status = 'CHECKOUT' OR bl.status = 'OVERDUE')
        """)
    Long countActiveBookLoanByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(bl) FROM BookLoan bl
        WHERE bl.user.id = :userId AND
         bl.status = 'OVERDUE'
        """)
    Long countOverdueBookLoanByUserId(@Param("userId") Long userId);


    @Query("""
        SELECT bl FROM BookLoan bl WHERE bl.dueDate <= :today\s
        AND (bl.status = 'CHECKOUT' OR bl.status = 'OVERDUE')
 \s
   \s""")
    Page<BookLoan> findOverdueBookLoans(@Param("today") LocalDate today, Pageable pageable);

    Page<BookLoan> findByUserId(Long userId, Pageable pageable);

    Page<BookLoan> findByBookId(Long bookId, Pageable pageable);

    Page<BookLoan> findByStatus(BookLoanStatus status, Pageable pageable);

    @Query("""
        SELECT bl FROM BookLoan bl WHERE bl.checkoutDate BETWEEN :startDate AND :endDate
    """)
    Page<BookLoan> findBookLoansByDataRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    Page<BookLoan> findByStatusAndUser(BookLoanStatus status, User currentUser, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM BookLoan l " +
            "WHERE l.user.id = :userId AND l.book.id = :bookId AND l.status = :status")
    boolean existsByUserIdAndBookIdAndStatus(
            @Param("userId") Long userId,
            @Param("bookId") Long bookId,
            @Param("status") BookLoanStatus activeStatuses
    );

}
