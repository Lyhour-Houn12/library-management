package com.library.application.entity;

import com.library.application.domain.FineStatus;
import com.library.application.domain.FineType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "fines")
public class Fine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Book Loan is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_loan_id", nullable = false)
    private BookLoan bookLoan;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Fine type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "fine_type")
    private FineType fineType;

    @NotNull(message = "Amount is required")
    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "amount_paid", nullable = false)
    private Double amountPaid = 0D;


    @NotNull(message = "Fine status is mandatory")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FineStatus status = FineStatus.PENDING;

    @Column(length = 500)
    private String reason;

    @Column(length = 1000)
    private String notes;

    // Waiver tracking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waived_by_user_id")
    private User waivedBy;

    @Column(name = "waived_at")
    private LocalDateTime waivedAt;

    @Column(name = "waiver_reason", length = 500)
    private String waiverReason;

    // Payment tracking
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_user_id")
    private User processedBy;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Calculate the outstanding amount for this fine
     * @return Amount still owed
     */
    public Double getAmountOutstanding() {
        return Math.max(0D, amount - amountPaid);
    }

    public void applyPayment(Double paymentAmount) {
        if(paymentAmount == null || paymentAmount < 0) {
            throw new IllegalArgumentException("Payment must be greater than zero");
        }
        double outStanding = getAmountOutstanding();

        if(paymentAmount > outStanding) {
            throw new IllegalArgumentException("Payment amount exceeds outstanding balance");
        }

        this.amountPaid += paymentAmount;

        if(this.amountPaid >= this.amount){
            this.amountPaid = this.amount;
            status = FineStatus.PAID;
            paidAt = LocalDateTime.now();
        }else if(this.amountPaid > 0){
            status = FineStatus.PARTIAL_PAID;
        }

    }

    public void waive(User adminUser, String reason){
        this.waivedBy = adminUser;
        this.waivedAt = LocalDateTime.now();
        this.waiverReason = reason;
        this.status = FineStatus.WAIVED;
    }


}
