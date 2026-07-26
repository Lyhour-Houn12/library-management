package com.library.application.service.impl;

import com.library.application.domain.FineStatus;
import com.library.application.domain.PaymentGateway;
import com.library.application.domain.PaymentType;
import com.library.application.entity.BookLoan;
import com.library.application.entity.Fine;
import com.library.application.entity.User;
import com.library.application.exception.FineException;
import com.library.application.mapper.FineMapper;
import com.library.application.payload.dto.FineDTO;
import com.library.application.payload.request.CreateFineRequest;
import com.library.application.payload.request.PaymentInitiateRequest;
import com.library.application.payload.response.PaymentInitiateResponse;
import com.library.application.repository.BookLoanRepository;
import com.library.application.repository.FineRepository;
import com.library.application.service.FineService;
import com.library.application.service.PaymentService;
import com.library.application.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FineServiceImpl implements FineService {
    private final UserService userService;
    private final FineRepository fineRepository;
    private final PaymentService paymentService;
    private final BookLoanRepository bookLoanRepository;
    private final FineMapper fineMapper;

    @Override
    public FineDTO createFine(CreateFineRequest request) {
        BookLoan bookLoan = bookLoanRepository.findById(request.getBookLoanId())
                .orElseThrow(() -> new FineException("Book Loan Not Found"));

        User currentUser = userService.getCurrentUser();
        Fine fine = Fine.builder()
                .bookLoan(bookLoan)
                .user(currentUser)
                .fineType(request.getType())
                .amount(request.getAmount())
                .amountPaid(0.0)
                .status(FineStatus.PENDING)
                .notes(request.getNotes())
                .reason(request.getReason())
                .build();

        Fine savedFine = fineRepository.save(fine);

        return fineMapper.toDTO(savedFine);
    }

    @Override
    public PaymentInitiateResponse payFineFully(Long fineId, String transactionId) {
        User currentUser = userService.getCurrentUser();
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new FineException("Fine Not Found"));

        if(fine.getStatus().equals(FineStatus.PAID)) {
            throw new FineException("Payment has already been paid");
        }

        if(fine.getStatus().equals(FineStatus.WAIVED)){
            throw new FineException("Fine was waived by user: " + currentUser.getUsername());
        }


        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .userId(currentUser.getId())
                .fineId(fineId)
                .gateway(PaymentGateway.STRIPE)
                .paymentType(PaymentType.FINE)
                .fineAmount(fine.getAmount())
                .currency("USD")
                .description("Library fine payment for fine ID " + fine.getId())
                .build();
        return paymentService.initiatePayment(request);
    }

    @Override
    @Transactional
    public void markAsPaid(Long fineId, Double amount, String transactionId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new FineException("Fine Not Found"));

        // Apply payment amount safely
        fine.applyPayment(amount);
        fine.setTransactionId(transactionId);
        fine.setStatus(FineStatus.PAID);
        fine.setUpdatedAt(LocalDateTime.now());

        fineRepository.save(fine);

        log.info("Fine {} marked as fully paid (TXN_: {})", fineId, transactionId);

    }


}
