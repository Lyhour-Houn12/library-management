package com.library.application.payload.request;

import com.library.application.domain.PaymentGateway;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionRequest {
    private Long userId;

    @NotNull(message = "Plan ID is mandatory")
    private Long planId;

    private PaymentGateway paymentGateway;

    private Boolean autoRenew = false;

    private String successUrl;

    private String cancelUrl;
}
