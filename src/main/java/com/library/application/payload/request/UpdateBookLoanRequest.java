package com.library.application.payload.request;


import com.library.application.domain.BookLoanStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookLoanRequest {
    private BookLoanStatus status;

    private LocalDate dueDate;

    private LocalDate returnDate;


    private Integer maxRenewals;

    private BigDecimal fineAmount;

    private Boolean finePaid;

    private String notes;
}
