package com.library.application.payload.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaiveFineRequest {
    @NotNull(message = "Fine id is required")
    private Long fineId;

    private String reason;
}
