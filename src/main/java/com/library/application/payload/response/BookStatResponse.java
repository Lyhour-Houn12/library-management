package com.library.application.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookStatResponse {
    private Long totalActiveBooks;
    private Long totalAvailableBooks;
    private Long totalUnAvailableBooks;
    private Long totalInActiveBooks;
}
