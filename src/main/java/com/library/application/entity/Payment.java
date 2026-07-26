package com.library.application.entity;

import com.library.application.domain.PaymentGateway;
import com.library.application.domain.PaymentStatus;
import com.library.application.domain.PaymentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_loan_id")
    private BookLoan bookLoan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fine_id")
    private Fine fine;

    @NotNull(message = "Payment type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @NotNull(message = "Payment gateway is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_gateway", nullable = false)
    private PaymentGateway gateway;

    @NotNull(message = "Payment status is mandatory")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive number")
    private Double amount;

    @Column(length = 3, nullable = false)
    private String currency = "USD";

    @Column(name = "transaction_id", length = 255)
    private String transactionId;

    /**
     * Gateway payment ID (for successful payments)
     */
    @Column(name = "gateway_payment_id", length = 255)
    private String gatewayPaymentId;


    @Column(name = "gateway_order_id", length = 255)
    private String gatewayOrderId;

    @Column(name = "gateway_signature")
    private String gatewaySignature;

    @Column(name = "payment_method")
    private String paymentMethod;


    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "initiated_at", nullable = false)
    private LocalDateTime initiatedAt;

    /**
     * Date and time when payment was completed
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    /**
     * Whether notification has been sent
     */
    @Column(name = "notification_sent", nullable = false)
    private Boolean notificationSent = false;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isSuccess(){
        return status == PaymentStatus.SUCCESS;
    }

    public boolean canRetry(){
        return (status == PaymentStatus.FAILED || status == PaymentStatus.CANCELLED) && retryCount < 3;
    }

    public boolean isPending(){
        return (status == PaymentStatus.PENDING || status == PaymentStatus.PROCESSING) ;
    }



}
