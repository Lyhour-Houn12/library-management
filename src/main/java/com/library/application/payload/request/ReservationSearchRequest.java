package com.library.application.payload.request;


import com.library.application.domain.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationSearchRequest {
    // User filter
    private Long userId;

    // Book filter
    private Long bookId;

    // Status filter
    private ReservationStatus status;

    // Active only (PENDING or AVAILABLE)
    private Boolean activeOnly = false;

    // Pagination
    private Integer page = 0;
    private Integer size = 20;

    // Sorting
    private String sortBy = "reservedAt"; // reservedAt, availableAt, queuePosition, status
    private String sortDirection = "DESC"; // ASC or DESC

    public Integer getPage() {
        return page != null ? page : 0;
    }

    public Integer getSize() {
        return size != null ? size : 20;
    }

    public String getSortBy() {
        return sortBy != null ? sortBy : "reservedAt";
    }

    public String getSortDirection() {
        return sortDirection != null ? sortDirection : "DESC";
    }

    public Boolean getActiveOnly() {
        return activeOnly != null ? activeOnly : false;
    }
}
