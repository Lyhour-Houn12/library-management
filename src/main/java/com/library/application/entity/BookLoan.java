package com.library.application.entity;

import com.library.application.domain.BookLoanStatus;
import com.library.application.domain.BookLoanType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "book_loans")
public class BookLoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @NotNull(message = "Book is required")
    @ManyToOne
    @JoinColumn(nullable = false, name = "book_id")
    private Book book;

    @NotNull(message = "Book loan type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookLoanType type;


    @NotNull(message = "Book status type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookLoanStatus status;

    @NotNull(message = "Checkout date is required")
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkoutDate;

    @NotNull(message = "Due date is required")
    @Column(name = "due_date",nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "renewal_count", nullable = false)
    private Integer renewalCount = 0;

    @Column(name = "max_renewal", nullable = false)
    private Integer maxRenewal = 2;

    @Column(length = 500)
    private String notes;

    @Column(name = "is_overdue", nullable = false)
    private Boolean isOverdue = false;

    @Column(name = "over_due_days")
    private Integer overdueDays = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public boolean isActive(){
        return status == BookLoanStatus.CHECKOUT || status == BookLoanStatus.OVERDUE;
    }
    public boolean canRenew(){
        return status == BookLoanStatus.CHECKOUT && !isOverdue && renewalCount <= maxRenewal;
    }
}
