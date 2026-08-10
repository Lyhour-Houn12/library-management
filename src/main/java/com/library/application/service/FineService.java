package com.library.application.service;

import com.library.application.domain.FineStatus;
import com.library.application.domain.FineType;
import com.library.application.payload.dto.FineDTO;
import com.library.application.payload.dto.WaiveFineRequest;
import com.library.application.payload.request.CreateFineRequest;
import com.library.application.payload.response.PageResponse;
import com.library.application.payload.response.PaymentInitiateResponse;

import java.util.List;

public interface FineService {

    FineDTO createFine(CreateFineRequest request);

    PaymentInitiateResponse payFineFully(Long fineId, String transactionId);

    void markAsPaid(Long fineId, String transactionId);


    FineDTO waiveFine(WaiveFineRequest request);


    FineDTO getFineById(Long fineId);

    List<FineDTO> getFineByBookLoanId(Long bookLoanId);

    List<FineDTO> getMyFines(FineStatus status, FineType type);

    PageResponse<FineDTO> getAllFines(FineStatus status, FineType type, Long userId, int page, int size);


    Long getMyTotalUnpaid();

    Long getTotalUnpaidFineByUserId(Long userId);

    Long getOutStandingFine();

    // ==================== VALIDATION OPERATIONS ====================

    /**
     * Check if a user has any unpaid fines
     * @param userId User ID
     * @return true if user has unpaid fines
     */
    boolean hasUnpaidFines(Long userId);

    /**
     * Delete a fine (admin only - use with caution)
     * @param fineId Fine ID
     */
    void deleteFine(Long fineId);
}
