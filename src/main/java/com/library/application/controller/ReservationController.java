package com.library.application.controller;

import com.library.application.domain.ReservationStatus;
import com.library.application.payload.dto.ReservationDTO;
import com.library.application.payload.request.ReservationRequest;
import com.library.application.payload.request.ReservationSearchRequest;
import com.library.application.payload.response.ApiResponse;
import com.library.application.payload.response.PageResponse;
import com.library.application.service.ReservationService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<ReservationDTO> createReservationForUser(@Valid @RequestBody ReservationRequest reservationRequest) {
        return new ResponseEntity<>(reservationService.createReservation(reservationRequest), HttpStatus.CREATED);
    }

    @PostMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createReservationForUser(@PathVariable Long userId, @Valid @RequestBody ReservationRequest request){
        return new ResponseEntity<>(reservationService.createReservationForUser(userId,  request), HttpStatus.CREATED);
    }

    @DeleteMapping("/cancel/{reservationId}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId) {
        return new ResponseEntity<>(reservationService.cancelReservation(reservationId), HttpStatus.OK);
    }

    @PostMapping("/fulfill/{reservationId}")
    public ResponseEntity<?> fulfillReservation(@PathVariable Long reservationId) {
        return new ResponseEntity<>(reservationService.fulfilledReservation(reservationId), HttpStatus.OK);
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<?> getReservationById(@PathVariable Long reservationId) {
        return new ResponseEntity<>(reservationService.getReservationById(reservationId), HttpStatus.OK);


    }
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ReservationDTO>> advancedSearchReservations(
            @RequestBody ReservationSearchRequest searchRequest) {
        PageResponse<ReservationDTO> reservations = reservationService.searchReservations(searchRequest);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/my")
    public ResponseEntity<PageResponse<ReservationDTO>> getMyReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "reservedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        ReservationSearchRequest searchRequest = new ReservationSearchRequest();
        searchRequest.setStatus(status);
        searchRequest.setActiveOnly(activeOnly);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);

        PageResponse<ReservationDTO> reservations = reservationService.getMyReservations(searchRequest);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/queue-position/{reservationId}")
    public ResponseEntity<?> getQueuePositions(@PathVariable Long reservationId) {
        Integer queuePosition = reservationService.getQueuePosition(reservationId);
        return ResponseEntity.ok(new QueuePositionResponse(queuePosition));
    }

    public ResponseEntity<?> processNextReservation(@PathVariable Long bookId){
        reservationService.processNextReservation(bookId);
        return ResponseEntity.ok(new ApiResponse("Update process successfully", true));
    }


    @Getter
    public static class QueuePositionResponse{
        private int  queuePosition;
        private String message;
        public QueuePositionResponse(Integer queuePosition) {
            this.queuePosition = queuePosition;
            if(queuePosition == 0){
                this.message = "Reservation is not in queue";
            }else if(queuePosition == 1){
                this.message = "You are next in line!";
            }else{
                this.message = "There are " + (queuePosition - 1) + " person(s) ahead you";
            }
        }

        public void setQueuePosition(int queuePosition) {
            this.queuePosition = queuePosition;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
