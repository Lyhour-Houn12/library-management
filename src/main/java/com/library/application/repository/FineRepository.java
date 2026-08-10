package com.library.application.repository;

import com.library.application.domain.FineStatus;
import com.library.application.domain.FineType;
import com.library.application.entity.BookLoan;
import com.library.application.entity.Fine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FineRepository extends JpaRepository<Fine, Long> {


    @Query("""
            SELECT f FROM Fine f WHERE f.bookLoan.id = :bookLoanId
            ORDER BY f.createdAt ASC
       """)
    List<Fine> findByBookLoanId(@Param("bookLoanId") Long bookLoanId);

    List<Fine> findByUserId(Long userId);

    @Query("""
        SELECT f FROM Fine f\s
        WHERE (:userId IS NULL OR f.user.id = :userId)
                AND (:fineStatus IS NULL OR f.status = :fineStatus)
                        AND(:fineType IS NULL OR f.fineType = :fineType)
        ORDER BY f.createdAt DESC
       \s""")
    Page<Fine> findAllWithFilter(@Param("userId") Long userId,@Param("fineStatus") FineStatus status,@Param("fineType") FineType type, Pageable pageable);


    @Query("SELECT COALESCE(SUM(f.amount - f.amountPaid), 0) FROM Fine f WHERE f.user.id = :userId AND f.status IN('PENDING', 'PARTIAL_PAID')")
    Long getTotalUnpaidFineByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(f.amount - f.amountPaid), 0) FROM Fine f WHERE f.status IN('PENDING', 'PARTIAL_PAID')")
    Long getTotalOutStandingFines();


    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Fine f WHERE f.user.id = :userId AND f.status IN('PENDING', 'PARTIAL_PAID')")
    boolean hasUniqueFines(@Param("userId") Long userId);

    Optional<Fine> findByBookLoanAndFineType(BookLoan bookLoan, FineType fineType);
}
