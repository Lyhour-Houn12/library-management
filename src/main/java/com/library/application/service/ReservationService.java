package com.library.application.service;

import com.library.application.payload.dto.ReservationDTO;
import com.library.application.payload.request.ReservationRequest;
import com.library.application.payload.request.ReservationSearchRequest;
import com.library.application.payload.response.PageResponse;

public interface ReservationService {
    ReservationDTO createReservation(ReservationRequest request);

    ReservationDTO createReservationForUser(Long userId, ReservationRequest request);

    ReservationDTO cancelReservation(Long reservationId);

    ReservationDTO fulfilledReservation(Long reservationId);

    ReservationDTO getReservationById(Long reservationId);

    PageResponse<ReservationDTO> searchReservations(ReservationSearchRequest searchRequest);

    PageResponse<ReservationDTO> getMyReservations(ReservationSearchRequest searchRequest);

    Integer getQueuePosition(Long reservationId);

    // ==================== ADMIN OPERATIONS ====================

    void processNextReservation(Long bookId);

    Integer expiredOldReservation();

    void updateQueuePosition(Long bookId);

}
