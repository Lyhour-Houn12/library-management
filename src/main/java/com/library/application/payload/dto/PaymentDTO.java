package com.library.application.payload.dto;

import com.library.application.domain.PaymentGateway;
import com.library.application.domain.PaymentStatus;
import com.library.application.domain.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    private Long id;

    @NotNull(message = "User is required")
    private Long userId;

    private String username;

    private String userEmail;

    private Long bookLoanId;

    private Long subscriptionId;
    @NotNull(message = "Payment type is mandatory")
    private PaymentType paymentType;

    private PaymentStatus status;

    @NotNull(message = "Payment gateway is mandatory")
    private PaymentGateway gateway;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @Size(min = 3, max = 3, message = "Currency must be 3-letter code")
    private String currency;

    private String transactionId;

    private String gatewayPaymentId;

    private String gatewayOrderId;

    private String gatewaySignature;

    private String paymentMethod;

    private String description;

    private String failureReason;

    private Integer retryCount;

    private LocalDateTime initiatedAt;

    private LocalDateTime completedAt;

    private Boolean notificationSent;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
