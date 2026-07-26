package com.library.application.event;

import com.library.application.domain.PaymentGateway;
import com.library.application.domain.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain event published when a payment is initiated.
 * This event can be used for tracking, analytics, and notifications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitiatedEvent {

    private Long paymentId;

    private Long userId;

    private PaymentType paymentType;

    private PaymentGateway gateway;

    private Double amount;

    private String currency;

    private Long subscriptionId;

    private Long bookLoanId;

    private Long fineId;

    private String transactionId;

    private String description;

    private LocalDateTime initiatedAt;

    private String checkoutUrl;

    private String userEmail;

    private String userName;
}
