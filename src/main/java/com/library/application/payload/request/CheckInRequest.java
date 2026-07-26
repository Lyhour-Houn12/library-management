package com.library.application.payload.request;

import com.library.application.domain.BookLoanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckInRequest {

    @NotNull(message = "Book loan is required")
    private Long bookLoanId;

    private BookLoanStatus condition = BookLoanStatus.RETURNED;

    private String notes;
}
