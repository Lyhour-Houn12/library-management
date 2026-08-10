package com.library.application.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WishlistDTO {

    private Long id;
    private Long userId;
    private String username;
    private BookDTO book;
    private LocalDateTime addedAt;
    private String notes;
}
