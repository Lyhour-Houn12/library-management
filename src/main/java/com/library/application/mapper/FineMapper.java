package com.library.application.mapper;

import com.library.application.domain.FineStatus;
import com.library.application.domain.FineType;
import com.library.application.entity.BookLoan;
import com.library.application.entity.Fine;
import com.library.application.entity.User;
import com.library.application.exception.BookLoanException;
import com.library.application.exception.UserException;
import com.library.application.payload.dto.FineDTO;
import com.library.application.repository.BookLoanRepository;
import com.library.application.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class FineMapper {
    private final UserRepository userRepository;
    private final BookLoanRepository bookLoanRepository;

    public FineDTO toDTO(Fine fine) {
        if(fine == null) return null;

        FineDTO dto = new FineDTO();

        dto.setId(fine.getId());


        // Book Information
        if(fine.getBookLoan() != null) {
            dto.setBookLoanId(fine.getBookLoan().getId());
            if(fine.getBookLoan().getBook() != null){
                dto.setBookTitle(fine.getBookLoan().getBook().getTitle());
                dto.setBookIsbn(fine.getBookLoan().getBook().getIsbn());
            }
        }

        if(fine.getUser() != null) {
            dto.setUserId(fine.getUser().getId());
            dto.setUserName(fine.getUser().getUsername());
            dto.setUserEmail(fine.getUser().getEmail());
        }
        dto.setFineType(fine.getFineType());
        dto.setAmount(fine.getAmount());
        dto.setAmountPaid(fine.getAmountPaid());
        dto.setAmountOutStanding(fine.getAmountOutstanding());
        dto.setStatus(fine.getStatus());
        dto.setReason(fine.getReason());
        dto.setNotes(fine.getNotes());

        if(fine.getWaivedBy() != null) {
            dto.setWaivedByUserId(fine.getWaivedBy().getId());
            dto.setWaivedByUserName(fine.getWaivedBy().getUsername());
        }
        dto.setWaivedAt(fine.getWaivedAt());
        dto.setWaiverReason(fine.getWaiverReason());

        dto.setPaidAt(fine.getPaidAt());

        if(fine.getProcessedBy() != null) {
            dto.setProcessedByUserId(fine.getProcessedBy().getId());
            dto.setProcessedByUserName(fine.getProcessedBy().getUsername());
        }
        dto.setTransactionId(fine.getTransactionId());
        dto.setCreatedAt(fine.getCreatedAt());
        dto.setUpdatedAt(fine.getUpdatedAt());

        return dto;
    }

    public Fine toEntity(FineDTO dto) {
        if(dto == null) return null;

        Fine fine = new Fine();

        if(dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new UserException("User not found"));
            fine.setUser(user);
        }
        if(dto.getBookLoanId() != null) {
            BookLoan bookLoan = bookLoanRepository.findById(dto.getBookLoanId())
                    .orElseThrow(() -> new BookLoanException("Book Loan not found"));
            fine.setBookLoan(bookLoan);
        }
        fine.setFineType(dto.getFineType());
        fine.setAmount(dto.getAmount());
        fine.setAmountPaid(dto.getAmountPaid() != null ? dto.getAmountPaid() : 0.0);
        fine.setStatus(dto.getStatus() != null ? dto.getStatus() : FineStatus.PENDING);
        fine.setReason(dto.getReason());
        fine.setNotes(dto.getNotes());
        fine.setTransactionId(dto.getTransactionId());
        return fine;
    }


}
