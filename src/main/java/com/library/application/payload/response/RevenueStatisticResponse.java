package com.library.application.payload.response;

import lombok.Data;

@Data
public class RevenueStatisticResponse {
    private double monthlyRevenue;
    private String currency;
    private int year;
    private int month;
    private int day;
}
