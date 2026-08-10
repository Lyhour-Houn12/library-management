package com.library.application.payload.dto;

import com.library.application.domain.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDTO {
    private Long id;

    private Long userId;
    private String username;
    private String userEmail;

    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private String bookAuthor;
    private Boolean isBookAvailable;

    private ReservationStatus status;
    private LocalDateTime reservedAt;
    private LocalDateTime availableAt;
    private LocalDateTime availableUntil;
    private LocalDateTime fulfilledAt;
    private LocalDateTime cancelledAt;
    private Integer queuePosition;
    private Boolean notificationSent;
    private String notes;


    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    // computed field;

    private Boolean isExpired;
    private Boolean canBeCancelled;
    private Long hourUntilExpiry; // Hour remains pick up


}
