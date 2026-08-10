package com.library.application.entity;

import com.library.application.domain.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required for reservation")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Book is required for reservation")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Reservation status is required")
    @Column(name = "reservation_status", nullable = false)
    private ReservationStatus status;


    /**
     * Date and time when reservation was created
     */
    @NotNull
    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    /**
     * Date and time when book became available for pickup
     */
    @Column(name = "available_at")
    private LocalDateTime availableAt;

    /**
     * Date and time until which the book will be held (after becoming available)
     * Usually 48-72 hours after notification
     */
    @Column(name = "available_until")
    private LocalDateTime availableUntil;

    /**
     * Date and time when reservation was fulfilled (book checked out)
     */
    @Column(name = "fulfilled_at")
    private LocalDateTime fulfilledAt;

    /**
     * Date and time when reservation was cancelled or expired
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "queue_position")
    private Integer queuePosition;

    /**
     * Whether notification email has been sent when book became available
     */
    @Column(name = "notification_sent", nullable = false)
    private Boolean notificationSent = false;

    /**
     * Additional notes or reason for cancellation
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public Boolean isActive(){
        return status == ReservationStatus.PENDING ||  status == ReservationStatus.AVAILABLE;
    }

    public Boolean canBeCancelled(){
        return status == ReservationStatus.PENDING || status == ReservationStatus.AVAILABLE;
    }

    /**
     * Check if reservation has expired
     */
    public Boolean hasExpired(){
        return status == ReservationStatus.AVAILABLE
                && availableUntil != null && availableAt.isAfter(LocalDateTime.now());
    }

}
