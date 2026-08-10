package com.library.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
@Table(name = "book_reviews")
public class BookReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @NotNull(message = "Book is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    @Column(nullable = false)
    private Integer rating;

    @NotBlank(message = "Review text is required")
    @Size(min = 10, max = 2000, message = "Review must be between 10 and 2000 characters")
    @Column(nullable = false, length = 2000)
    private String reviewText;

    @Size(max = 200, message = "Review title must not exceed 200 characters")
    @Column(length = 200)
    private String title;

    @Column(nullable = false)
    private Boolean isVerifiedReader = false; // True if user has completed a loan for this book

    @Column(nullable = false)
    private Boolean isActive = true; // For soft delete/moderation

    @Column(name = "helpful_count", nullable = false)
    private Integer helpfulCount = 0; // Number of users who found this review helpful

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;



}
