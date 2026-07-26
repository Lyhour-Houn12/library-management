package com.library.application.controller;

import com.library.application.payload.dto.PaymentDTO;
import com.library.application.payload.request.PaymentInitiateRequest;
import com.library.application.payload.request.PaymentVerifyRequest;
import com.library.application.payload.response.PaymentInitiateResponse;
import com.library.application.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/initiated")
    public ResponseEntity<?> initiatedPayment(@Valid @RequestBody PaymentInitiateRequest request){
        return new ResponseEntity<>(paymentService.initiatePayment(request), HttpStatus.CREATED);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@Valid @RequestBody PaymentVerifyRequest request){
        return new ResponseEntity<>(paymentService.verifyPayment(request), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPayment(@PathVariable Long paymentId){
        return new ResponseEntity<>(paymentService.getPaymentById(paymentId), HttpStatus.OK);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getPaymentByTransactionId(@PathVariable String transactionId){
        return new ResponseEntity<>(paymentService.getPaymentByTransactionId(transactionId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<?> getAllPayments(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(defaultValue = "createdAt") String sortBy,
                                            @RequestParam(defaultValue = "DESC") String sortDir){
        Sort sort = sortDir.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return new ResponseEntity<>(paymentService.getAllPayments(pageable), HttpStatus.OK);
    }


    /**
     * Cancel a pending payment
     * PUT /api/payments/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelPayment(@PathVariable Long id) {
            PaymentDTO payment = paymentService.cancelPayment(id);
            return ResponseEntity.ok(payment);
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<?> retryPayment(@PathVariable Long id) {
            PaymentInitiateResponse response = paymentService.retryPayment(id);
            return ResponseEntity.ok(response);
    }

    /**
     * Stripe webhook endpoint
     * POST /api/payments/webhook/stripe
     *
     * stripe sends payment notifications to this endpoint
     * Events: payment.captured, payment_link.paid, payment.failed
     */
}
