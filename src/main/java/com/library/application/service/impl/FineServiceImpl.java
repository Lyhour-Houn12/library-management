package com.library.application.service.impl;

import com.library.application.domain.*;
import com.library.application.entity.BookLoan;
import com.library.application.entity.Fine;
import com.library.application.entity.Payment;
import com.library.application.entity.User;
import com.library.application.exception.FineException;
import com.library.application.mapper.FineMapper;
import com.library.application.payload.dto.FineDTO;
import com.library.application.payload.dto.WaiveFineRequest;
import com.library.application.payload.request.CreateFineRequest;
import com.library.application.payload.request.PaymentInitiateRequest;
import com.library.application.payload.response.PageResponse;
import com.library.application.payload.response.PaymentInitiateResponse;
import com.library.application.repository.BookLoanRepository;
import com.library.application.repository.FineRepository;
import com.library.application.repository.PaymentRepository;
import com.library.application.service.FineService;
import com.library.application.service.PaymentService;
import com.library.application.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FineServiceImpl implements FineService {
    private final UserService userService;
    private final FineRepository fineRepository;
    private final PaymentService paymentService;
    private final BookLoanRepository bookLoanRepository;
    private final FineMapper fineMapper;
    private final PaymentRepository paymentRepository;

    @Override
    public FineDTO createFine(CreateFineRequest request) {
        BookLoan bookLoan = bookLoanRepository.findById(request.getBookLoanId())
                .orElseThrow(() -> new FineException("Book Loan Not Found"));

        Fine fine = Fine.builder()
                .bookLoan(bookLoan)
                .user(bookLoan.getUser())
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

        boolean isOwner = fine.getBookLoan().getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().equals(UserRole.ROLE_ADMIN);


        if(!isOwner && !isAdmin) {
            throw new FineException("You are not authorized to pay this fine");
        }


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
                .fineAmount(fine.getAmountOutstanding())
                .currency("USD")
                .description("Library fine payment for fine ID " + fine.getId())
                .build();
        return paymentService.initiatePayment(request);
    }

    @Override
    @Transactional
    public void markAsPaid(Long fineId, String transactionId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new FineException("Fine Not Found"));

        if (fine.getStatus() == FineStatus.PAID) {
            log.info("Fine {} is already paid.", fineId);
            return;
        }

        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new FineException("Payment Not Found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new FineException("Payment has not been completed successfully");
        }



        // Apply payment amount safely
        fine.applyPayment(payment.getAmount());
        fine.setTransactionId(transactionId);

        fineRepository.save(fine);

        log.info("Fine {} marked as fully paid (TXN_: {})", fineId, transactionId);

    }

    @Override
    public FineDTO waiveFine(WaiveFineRequest request) {
        Fine fine = fineRepository.findById(request.getFineId())
                .orElseThrow(() -> new FineException("Fine Not Found"));

        // check if already waive or paid
        if(fine.getStatus().equals(FineStatus.PAID)){
            throw new FineException("Payment has already been paid");
        }
        if(fine.getStatus().equals(FineStatus.WAIVED)){
            throw new FineException("Fine was waived by user: " + fine.getUser().getUsername());
        }

        User adminUser = userService.getCurrentUser();
        fine.waive(adminUser, request.getReason());
        Fine waviedFine =  fineRepository.save(fine);
        return fineMapper.toDTO(waviedFine);
    }

    @Override
    public FineDTO getFineById(Long fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new FineException("Fine Not Found"));
        return fineMapper.toDTO(fine);
    }

    @Override
    public List<FineDTO> getFineByBookLoanId(Long bookLoanId) {
//        return fineRepository.findAll()
//                .stream()
//                .filter(fine -> fine.getBookLoan().getId().equals(bookLoanId))
//                .map(fineMapper::toDTO)
//                .collect(Collectors.toList()); // good but slow if many data come
        List<Fine> fines = fineRepository.findByBookLoanId(bookLoanId);
        if(fines.isEmpty()){
            throw new FineException("Fines Not Found");
        }
        return fines.stream().map(fineMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<FineDTO> getMyFines(FineStatus status, FineType type) {
        User currentUser = userService.getCurrentUser();
        List<Fine> fines;
        // Apply filters based on parameters
        if(status != null && type != null){
            fines = fineRepository.findByUserId(currentUser.getId())
                    .stream()
                    .filter(fine -> fine.getStatus().equals(status) && fine.getFineType().equals(type))
                    .toList();
        }else if(status != null){
            fines = fineRepository.findByUserId(currentUser.getId())
                    .stream()
                    .filter(fine -> fine.getStatus().equals(status))
                    .toList();
        }else if(type != null){
            fines = fineRepository.findByUserId(currentUser.getId())
                    .stream()
                    .filter(fine -> fine.getFineType().equals(type))
                    .toList();
        }else{
            fines = fineRepository.findByUserId(currentUser.getId());
        }

        return fines.stream().map(fineMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public PageResponse<FineDTO> getAllFines(FineStatus status, FineType type, Long userId, int page, int size) {
        page = Math.max(page, 1);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Fine> fines = fineRepository.findAllWithFilter(userId, status, type, pageable);
        return PageResponse.from(
                fines.map(fineMapper::toDTO)
        );
    }

    @Override
    public Long getMyTotalUnpaid() {
        User user = userService.getCurrentUser();
        return getTotalUnpaidFineByUserId(user.getId());
    }

    @Override
    public Long getTotalUnpaidFineByUserId(Long userId) {
        return fineRepository.getTotalUnpaidFineByUserId(userId);
    }

    @Override
    public Long getOutStandingFine() {
        return fineRepository.getTotalOutStandingFines();
    }

    @Override
    public boolean hasUnpaidFines(Long userId) {
        return fineRepository.hasUniqueFines(userId);
    }

    @Override
    public void deleteFine(Long fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new FineException("Fine not found with id: " + fineId));
        fineRepository.delete(fine);
        log.warn("Fine {} deleted", fineId);
    }


}
