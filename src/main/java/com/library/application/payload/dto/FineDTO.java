package com.library.application.payload.dto;

import com.library.application.domain.FineStatus;
import com.library.application.domain.FineType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FineDTO {

    private Long id;

    @NotNull(message = "Book loan ID is required")
    private Long bookLoanId;

    private String bookTitle;

    private String bookIsbn;

    @NotNull(message = "User ID is mandatory")
    private Long userId;

    private String userName;

    private String userEmail;

    @NotNull(message = "Fine type is required")
    private FineType fineType;


    @NotNull(message = "Fine amount is required")
    @PositiveOrZero(message = "Fine amount cannot be negative")
    private Double amount;


    @PositiveOrZero(message = "Amount paid cannot be negative")
    private Double amountPaid;

    private Double amountOutStanding;


    @NotNull(message = "Fine status is required")
    private FineStatus status;

    private String reason;

    private String notes;

    // Waiver information
    private Long waivedByUserId;

    private String waivedByUserName;

    private LocalDateTime waivedAt;

    private String waiverReason;

    // Payment information
    private LocalDateTime paidAt;

    private Long processedByUserId;

    private String processedByUserName;

    private String transactionId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
