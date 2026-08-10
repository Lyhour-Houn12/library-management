package com.library.application.payload.request;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReviewRequest {
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1 character")
    @Max(value = 5, message = "Rating must not be exceeded over 5 characters")
    private Integer rating;

    @NotBlank(message = "Review text is required")
    @Size(min = 1, max = 2000, message = "Review must be between 1 and 2000 characters")
    private String reviewText;


    @Size(max = 200, message = "Review title must not exceed 200 characters")
    private String title;
}
