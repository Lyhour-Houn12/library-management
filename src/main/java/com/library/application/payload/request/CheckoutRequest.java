package com.library.application.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequest {

    @NotNull(message = "Book is required")
    private Long bookId;

    @Min(value = 1, message = "Checkout day must be at least 1")
    private Integer checkoutDate;

    private String notes;
}
