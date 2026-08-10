package com.library.application.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRatingStatisticsDTO {

    private Long bookId;

    private String bookTitle;

    private Double averageRating;

    private Long totalReviews;

    // Rating distribution: key = rating (1-5), value = count
    private Map<Integer, Long> ratingDistribution;

    private Long verifiedReaderReviews;
}