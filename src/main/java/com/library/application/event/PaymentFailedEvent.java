package com.library.application.event;


import com.library.application.domain.PaymentStatus;
import com.library.application.domain.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentFailedEvent {
    private Long paymentId;

    private Long userId;

    private String failureReason;

    private PaymentType paymentType;

    private Double amount;

    private String currency;

    private Long subscriptionId;

    private Long bookLoanId;

    private Long finalLoanId;

    private String gatewayPaymentId;

    private LocalDateTime failedAt;

    private String description;

    private String userEmail;

    private String userName;
}
