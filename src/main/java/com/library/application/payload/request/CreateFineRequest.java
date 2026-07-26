package com.library.application.payload.request;


import com.library.application.domain.FineType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateFineRequest {

    @NotNull(message = "Book loan is required")
    private Long bookLoanId;

    @NotNull(message = "Fine type is required")
    private FineType type;

    @NotNull(message = "Amount is required")
    @PositiveOrZero(message = "Fine amount must be positive")
    private Double amount;

    private String reason;

    private String notes;

}
