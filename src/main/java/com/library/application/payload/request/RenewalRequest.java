package com.library.application.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RenewalRequest {
    private Long bookLoanId;

    private Integer extensionDays;

    private String notes;
}
