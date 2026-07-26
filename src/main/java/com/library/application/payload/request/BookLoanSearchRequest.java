package com.library.application.payload.request;

import com.library.application.domain.BookLoanStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookLoanSearchRequest {
    private Long userId;
    private Long bookId;
    private BookLoanStatus status;
    private Boolean overDueOnly;
    private Boolean unpaidFineOnly;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
