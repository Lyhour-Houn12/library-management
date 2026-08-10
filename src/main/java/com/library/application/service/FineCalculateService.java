package com.library.application.service;

import com.library.application.entity.BookLoan;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class FineCalculateService {

    private static final BigDecimal FINE_PER_DAY = new  BigDecimal("1.00");
    private static final BigDecimal MAXIMUM_FINE = new BigDecimal("50.00");
    private static final BigDecimal LOST_BOOK_PENALTY = new BigDecimal("100.00");
    private static final BigDecimal DAMAGE_BOOK_PENALTY = new BigDecimal("25.00");
    private static final int GRACE_PERIOD_DAYS = 0;

    public BigDecimal calculateOverdueFine(BookLoan bookLoan) {
        if(bookLoan.getReturnDate() == null){
            return calculateFine(bookLoan.getDueDate(), LocalDate.now());
        }else{
            return calculateFine(bookLoan.getReturnDate(), bookLoan.getReturnDate());
        }
    }

    /**
     * Calculate fine between two dates
     * @param dueDate The due date
     * @param actualDate The actual return date or current date
     * @return The calculated fine amount
     */
    public BigDecimal calculateFine(LocalDate dueDate, LocalDate actualDate){
        if(actualDate.isBefore(dueDate) || actualDate.isEqual(dueDate)){
            return BigDecimal.ZERO;
        }
        long overdueDays = ChronoUnit.DAYS.between(dueDate, actualDate);
        if(overdueDays <= 0){
            return BigDecimal.ZERO;
        }

        BigDecimal fine = FINE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays));

        if(fine.compareTo(MAXIMUM_FINE) > 0){
            fine = MAXIMUM_FINE;
        }
        return fine;
    }


    /**
     * Calculate overdue days
     * @param dueDate The due date
     * @param actualDate The actual return date or current date
     * @return Number of overdue days
     */
    public int calculateOverdueDays(LocalDate dueDate, LocalDate actualDate){
        if(actualDate.isBefore(dueDate) || actualDate.isEqual(dueDate)){
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(dueDate, actualDate);
    }

}
