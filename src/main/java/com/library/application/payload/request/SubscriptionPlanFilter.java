package com.library.application.payload.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Data
@NoArgsConstructor
public class SubscriptionPlanFilter {
    private String searchTerm;
    private Boolean active;
    private Boolean featured;
    private String currency;
    private Integer minDuration;
    private Integer maxDuration;
    private Double  minPrice;
    private Double maxPrice;
    private Integer minBookAllowed;
    private Integer maxBookAllowed;
    private SubscriptionPlanSortField sortBy = SubscriptionPlanSortField.CREATED_AT;
    private Sort.Direction direction = Sort.Direction.ASC;
}
