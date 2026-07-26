package com.library.application.event;


import com.library.application.domain.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain event published when a payment is successfully completed.
 * This event decouples PaymentService from other domain services,
 * allowing them to react to payment success independently.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSuccessEvent {
    private Long paymentId;

    private Long userId;

    private PaymentType type;

    private Double amount;

    private String currency;

    private Long subscriptionId;

    private Long fineId;

    private Long bookLoanId;

    private String gatewayPaymentId;

    private String transactionId;

    private LocalDateTime completedAt;

    private String description;
}
