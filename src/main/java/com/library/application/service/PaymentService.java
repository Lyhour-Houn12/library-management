package com.library.application.service;

import com.library.application.payload.dto.PaymentDTO;
import com.library.application.payload.request.PaymentInitiateRequest;
import com.library.application.payload.request.PaymentVerifyRequest;
import com.library.application.payload.response.PageResponse;
import com.library.application.payload.response.PaymentInitiateResponse;
import com.library.application.payload.response.RevenueStatisticResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request);

    PaymentDTO verifyPayment(PaymentVerifyRequest request);

    PaymentDTO getPaymentById(Long paymentId);

    PaymentDTO getPaymentByTransactionId(String transactionId);

    Page<PaymentDTO> getAllPayments(Pageable pageable);

    Page<PaymentDTO> getUserPayments(Long userId, Pageable pageable);

    PaymentDTO cancelPayment(Long paymentId);

    PaymentInitiateResponse retryPayment(Long paymentId);

    RevenueStatisticResponse getMonthlyRevenue();



}
