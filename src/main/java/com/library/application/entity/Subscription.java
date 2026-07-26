package com.library.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    @NotNull(message = "Subscription plan is required")
    @ManyToOne
    @JoinColumn(nullable = false)
    private SubscriptionPlan plan;

    // copy attributes in subscription plan in order to catch historical subscription
    @Column(name = "plan_name", length = 100)
    private String planName;

    @Column(name = "plan_code", length = 100)
    private String planCode;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "price")
    private Double price;


    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @NotNull(message = "Max books allowed is mandatory")
    @Positive(message = "Max books must be positive")
    @Column(name = "max_books_allowed", nullable = false)
    private Integer maxBooksAllowed;

    @NotNull(message = "Max days allowed is mandatory")
    @Positive(message = "Max days must be positive")
    @Column(name = "days_allowed", nullable = false)
    private Integer maxDaysPerBook;

    /**
     * Auto-renewal flag
     */
    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew = false;


    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if subscription is currently valid
     */
    public boolean isValid(){
        if(!isActive){
            return false;
        }
        LocalDate today =  LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    public boolean isExpired(){
        return LocalDate.now().isAfter(endDate);
    }

    public Long getRemainingDays(){
        if(isExpired()){
            return 0L;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }

    public void calculateEndDate(){
        if(plan != null && startDate != null){
            this.endDate = startDate.plusDays(plan.getDurationDays());
        }
    }

    public void initializeFromPlan(){
        if(plan != null){
            this.planName = plan.getName();
            this.planCode = plan.getPlanCode();
            this.price = plan.getPrice();
            this.currency = plan.getCurrency();
            this.maxBooksAllowed = plan.getMaxBookAllowed();
            this.maxDaysPerBook = plan.getMaxDaysPerBook();
            this.notes = plan.getAdminNotes();
            if(startDate == null){
                this.startDate = LocalDate.now();
            }
            calculateEndDate();
        }
    }




}
