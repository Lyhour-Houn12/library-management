package com.library.application.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$",
            message = "ISBN format is invalid")
    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @NotBlank(message = "Title is mandatory")
    @Size(min = 1, max = 255, message = "Title must be between 1 to 255 characters")
    private String title;

    @NotBlank(message = "Author is mandatory")
    @Size(min = 1, max = 255, message = "Title must be between 1 to 255 characters")
    @Column(nullable = false)
    private String author;

    @NotNull(message = "Genre is mandatory")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    @Size(max = 100, message = "Publisher name must not exceed 100 characters")
    private String publisher;


    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(length = 20)
    private String language;

    @Min(value = 1, message = "Pages must be at least 1")
    @Max(value = 50000, message = "Pages must not exceed 50000")
    private Integer page;

    @Column(length = 2000)
    private String description;


    @Min(value = 0, message = "Total copies cannot be negative")
    @NotNull(message = "Total copies is mandatory")
    @Column(name = "total_copies", nullable = false)
    private Integer totalCopies;

    @Min(value = 0, message = "Available copies cannot be negative")
    @NotNull(message = "Available copies is mandatory")
    @Column(name = "available_copies", nullable = false)
    private  Integer availableCopies;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Column(name = "cover_image_url", length = 500)
    private String coverImage;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false )
    private LocalDateTime updatedAt;

    @AssertTrue(message = "Available copies cannot exceed total copies")
    public boolean isAvailableCopiesValid(){
        if(totalCopies == null || availableCopies == null){
            return true;
        }
        return totalCopies >= availableCopies;
}

}
