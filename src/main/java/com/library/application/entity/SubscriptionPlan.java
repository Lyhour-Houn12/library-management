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

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "subscription_plans")
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * Unique plan code (e.g., "MONTHLY", "QUARTERLY", "YEARLY")
     */
    @NotBlank(message = "Plan code is required")
    @Column(name = "plan_code", nullable = false, unique = true, length = 50)
    private String planCode;

    @NotBlank(message = "Plan name is required")
    @Column(nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price has to be positive")
    @Column(name = "price", nullable = false)
    private Double price;


    @NotBlank(message = "Currency is required")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";


    @NotNull(message = "Max books allowed is required")
    @Positive(message = "Max books allowed must be positive")
    @Column(name = "max_book_allowed", nullable = false)
    private Integer maxBookAllowed;

    @Column(name = "display_order")
    private Integer displayOrder = 0;


    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Whether this plan is featured/recommended
     */
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    /**
     * Badge text (e.g., "Best Value", "Most Popular")
     */
    @Column(name = "badge_text", length = 50)
    private String badgeText;


    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

}
