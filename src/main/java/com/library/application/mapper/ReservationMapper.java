package com.library.application.mapper;

import com.library.application.entity.Reservation;
import com.library.application.payload.dto.ReservationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReservationMapper {

    public ReservationDTO toDTO(Reservation reservation) {
        if(reservation == null) return null;
        ReservationDTO dto = new ReservationDTO();
        dto.setId(reservation.getId());

        if(reservation.getUser() != null){
            dto.setUserId(reservation.getUser().getId());
            dto.setUsername(reservation.getUser().getUsername());
            dto.setUserEmail(reservation.getUser().getEmail());
        }

        if(reservation.getBook() != null){
            dto.setBookId(reservation.getBook().getId());
            dto.setBookAuthor(reservation.getBook().getAuthor());
            dto.setBookIsbn(reservation.getBook().getIsbn());
            dto.setBookTitle(reservation.getBook().getTitle());
            dto.setAvailableAt(reservation.getAvailableAt());
        }

        dto.setStatus(reservation.getStatus());
        dto.setReservedAt(reservation.getReservedAt());
        dto.setNotes(reservation.getNotes());
        dto.setAvailableAt(reservation.getAvailableAt());
        dto.setAvailableUntil(reservation.getAvailableUntil());
        dto.setFulfilledAt(reservation.getFulfilledAt());
        dto.setCancelledAt(reservation.getCancelledAt());
        dto.setQueuePosition(reservation.getQueuePosition());
        dto.setNotificationSent(reservation.getNotificationSent());
        dto.setNotes(reservation.getNotes());
        dto.setCreatedAt(reservation.getCreatedAt());
        dto.setUpdatedAt(reservation.getUpdatedAt());

        // computed field
        dto.setIsExpired(reservation.hasExpired());
        dto.setCanBeCancelled(reservation.canBeCancelled());


        if(dto.getAvailableUntil() != null){
            LocalDateTime now = LocalDateTime.now();
            if(now.isBefore(reservation.getAvailableUntil())){
                Long hours = Duration.between(reservation.getAvailableUntil(), now).toHours();
                dto.setHourUntilExpiry(hours);
            }else{
                dto.setHourUntilExpiry(null);
            }
        }
        return dto;
    }






}
