package com.library.application.payload.request;

import com.library.application.domain.PaymentGateway;
import com.library.application.domain.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for initiating a payment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitiateRequest {

    @NotNull(message = "User is required")
    private Long userId;

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    private Long bookLoanId;

    @NotNull(message = "Payment gateway is required")
    private PaymentGateway gateway; // RAZORPAY or STRIPE


    private Double fineAmount;

    @Size(min = 3, max = 3, message = "Currency must be 3-letter code (e.g., INR, USD)")
    private String currency = "USD";

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Long fineId;
    private Long subscriptionId;

    // Return URLs for payment gateway redirects
    @Size(max = 500, message = "Success URL must not exceed 500 characters")
    private String successUrl;

    @Size(max = 500, message = "Cancel URL must not exceed 500 characters")
    private String cancelUrl;
}
