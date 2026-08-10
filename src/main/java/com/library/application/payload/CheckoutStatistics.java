package com.library.application.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CheckoutStatistics {

    private long totalCheckout;
    private long activeCheckout;
    private long overdueCheckout;
    private long totalReturns;
    private BigDecimal totalUnpaidFines;
    private long transactionsWithFines;
}
