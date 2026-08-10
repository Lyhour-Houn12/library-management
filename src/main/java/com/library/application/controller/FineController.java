package com.library.application.controller;

import com.library.application.domain.FineStatus;
import com.library.application.domain.FineType;
import com.library.application.payload.dto.FineDTO;
import com.library.application.payload.request.CreateFineRequest;
import com.library.application.payload.response.ApiResponse;
import com.library.application.payload.response.PageResponse;
import com.library.application.repository.FineRepository;
import com.library.application.service.FineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fines")
@RequiredArgsConstructor
public class FineController {
    private final FineService fineService;


    @PostMapping("/create-fine")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createFine(@Valid @RequestBody CreateFineRequest request){
        return new ResponseEntity<>(fineService.createFine(request), HttpStatus.CREATED);
    }

    @PostMapping("/{fineId}/pay")
    public ResponseEntity<?> payFineFully(@PathVariable Long fineId, @RequestParam(required = false) String transactionId){
        return ResponseEntity.ok(fineService.payFineFully(fineId, transactionId));
    }


    @PostMapping("/mark-fine")
    public ResponseEntity<?> markFineAsPaid(@RequestParam Long fineId, @RequestParam String transactionId){
        fineService.markAsPaid(fineId, transactionId);
        return ResponseEntity.accepted()
                .body(new ApiResponse("Fine has been marked as paid successfully!", true));
    }

    // ==================== QUERY OPERATIONS ====================

    /**
     * Get fine by ID
     * GET /api/fines/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getFineById(@PathVariable Long id) {
            FineDTO fine = fineService.getFineById(id);
            return ResponseEntity.ok(fine);
    }

    /**
     * Get fines for a book loan
     * GET /api/fines/book-loan/{bookLoanId}
     */
    @GetMapping("/book-loan/{bookLoanId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getFinesByBookLoanId(@PathVariable Long bookLoanId) {
            List<FineDTO> fines = fineService.getFineByBookLoanId(bookLoanId);
            return ResponseEntity.ok(fines);
    }

    /**
     * Get my fines (current user) with optional filters
     * GET /api/fines/my?status=PENDING&type=OVERDUE
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getMyFines(
            @RequestParam(required = false) FineStatus status,
            @RequestParam(required = false) FineType type) {
            List<FineDTO> fines = fineService.getMyFines(status, type);
            return ResponseEntity.ok(fines);
    }

    /**
     * Get all fines with filtering (Admin only)
     * GET /api/fines?status=PENDING&type=OVERDUE&userId=123&page=0&size=20
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllFines(
            @RequestParam(required = false) FineStatus status,
            @RequestParam(required = false) FineType type,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
            PageResponse<FineDTO> fines = fineService.getAllFines(status, type, userId, page, size);
            return ResponseEntity.ok(fines);
    }

    // ==================== AGGREGATION OPERATIONS ====================

    /**
     * Get my total unpaid fines (current user)
     * GET /api/fines/my/total-unpaid
     */
    @GetMapping("/my/total-unpaid")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getMyTotalUnpaidFines() {
            Long total = fineService.getMyTotalUnpaid();
            return ResponseEntity.ok(new TotalFinesResponse(total));

    }

    /**
     * Get total unpaid fines for a user (Admin only)
     * GET /api/fines/statistics/user/{userId}/unpaid
     */
    @GetMapping("/statistics/user/{userId}/unpaid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTotalUnpaidFinesByUserId(@PathVariable Long userId) {
            Long total = fineService.getTotalUnpaidFineByUserId(userId);
            return ResponseEntity.ok(new TotalFinesResponse(total));
    }



    @GetMapping("/statistics/outstanding")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTotalOutstandingFines() {

        Long total = fineService.getOutStandingFine();
        return ResponseEntity.ok(new TotalFinesResponse(total));

    }

    /**
     * Check if user has unpaid fines (Admin only)
     * GET /api/fines/statistics/user/{userId}/has-unpaid
     */
    @GetMapping("/statistics/user/{userId}/has-unpaid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> hasUnpaidFines(@PathVariable Long userId) {
        boolean hasUnpaid = fineService.hasUnpaidFines(userId);
        return ResponseEntity.ok(new HasUnpaidFinesResponse(hasUnpaid));

    }

    /**
     * Delete a fine (Admin only - use with caution)
     * DELETE /api/fines/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteFine(@PathVariable Long id) {
            fineService.deleteFine(id);
            return ResponseEntity.ok(new ApiResponse("Fine deleted successfully", true));
    }

    public static class TotalFinesResponse {
        public Long total;

        public TotalFinesResponse(Long total) {
            this.total = total;
        }
    }

    public static class HasUnpaidFinesResponse {
        public boolean hasUnpaidFines;

        public HasUnpaidFinesResponse(boolean hasUnpaidFines) {
            this.hasUnpaidFines = hasUnpaidFines;
        }
    }



}
