package com.library.application.mapper;

import com.library.application.entity.BookLoan;
import com.library.application.payload.dto.BookLoanDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class BookLoanMapper {

    public BookLoanDTO toDto(BookLoan bookLoan){
        if(bookLoan == null){
            return null;
        }
        BookLoanDTO dto = new BookLoanDTO();
        dto.setId(bookLoan.getId());

        // User information
        if(bookLoan.getUser() != null){
            dto.setUserId(bookLoan.getUser().getId());
            dto.setUserName(bookLoan.getUser().getUsername());
            dto.setUserEmail(bookLoan.getUser().getEmail());
        }
        // Book information
        if(bookLoan.getBook() != null){
            dto.setBookId(bookLoan.getBook().getId());
            dto.setBookTitle(bookLoan.getBook().getTitle());
            dto.setBookIsbn(bookLoan.getBook().getIsbn());
            dto.setBookAuthor(bookLoan.getBook().getAuthor());
            dto.setCoverageImage(bookLoan.getBook().getCoverImage());
        }

        // Book loan information
        dto.setType(bookLoan.getType());
        dto.setStatus(bookLoan.getStatus());
        dto.setCheckoutDate(bookLoan.getCheckoutDate());
        dto.setDueDate(bookLoan.getDueDate());
        dto.setRemainingDays(ChronoUnit.DAYS.between(LocalDate.now(), bookLoan.getDueDate()));
        dto.setReturnDate(bookLoan.getReturnDate());
        dto.setRenewalCount(bookLoan.getRenewalCount());
        dto.setMaxRenewals(bookLoan.getMaxRenewal());

        dto.setNotes(bookLoan.getNotes());
        dto.setIsOverdue(bookLoan.getIsOverdue());
        dto.setOverdueDays(bookLoan.getOverdueDays());
        dto.setCreatedAt(bookLoan.getCreatedAt());
        dto.setUpdatedAt(bookLoan.getUpdatedAt());
        return dto;
    }

}
