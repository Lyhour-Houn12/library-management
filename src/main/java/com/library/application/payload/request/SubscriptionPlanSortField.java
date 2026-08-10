package com.library.application.payload.request;

import lombok.Getter;

@Getter
public enum SubscriptionPlanSortField {

    NAME("name"),
    PRICE("price"),
    DURATION_DAYS("durationDays"),
    DISPLAY_ORDER("displayOrder"),
    CREATED_AT("createdAt");

    private final String fieldName;
    SubscriptionPlanSortField(String fieldName) {
        this.fieldName = fieldName;
    }
}
