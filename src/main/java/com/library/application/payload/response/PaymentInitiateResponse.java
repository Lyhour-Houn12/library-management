package com.library.application.payload.response;

import com.library.application.domain.PaymentGateway;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for payment initiation
 * Contains gateway-specific data needed by frontend to complete payment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitiateResponse {
    private Long paymentId;

    private PaymentGateway gateway;

    private String transactionId;

    private String checkoutUrl;

    private String stripeOrderId;

    private Double amount;

    private String currency;

    private String description;

    private String message;
    private boolean success;


}
