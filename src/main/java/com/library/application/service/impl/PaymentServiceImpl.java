package com.library.application.service.impl;

import com.library.application.domain.PaymentGateway;
import com.library.application.domain.PaymentStatus;
import com.library.application.domain.PaymentType;
import com.library.application.entity.*;
import com.library.application.event.PaymentFailedEvent;
import com.library.application.event.PaymentInitiatedEvent;
import com.library.application.event.PaymentSuccessEvent;
import com.library.application.exception.FineException;
import com.library.application.exception.PaymentException;
import com.library.application.exception.SubscriptionException;
import com.library.application.exception.UserException;
import com.library.application.mapper.PaymentMapper;
import com.library.application.payload.dto.PaymentDTO;
import com.library.application.payload.request.PaymentInitiateRequest;
import com.library.application.payload.request.PaymentVerifyRequest;
import com.library.application.payload.response.PaymentInitiateResponse;
import com.library.application.payload.response.PaymentLinkResponse;
import com.library.application.payload.response.RevenueStatisticResponse;
import com.library.application.repository.*;
import com.library.application.service.PaymentService;
import com.library.application.service.gateway.StripeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final StripeService stripeService;
    private final PaymentMapper paymentMapper;
    private final FineRepository fineRepository;
    private final BookLoanRepository bookLoanRepository;

    @Override
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) {
        log.info("Initiating payment for user: {}, type: {}, gateway: {}",
                request.getUserId(), request.getPaymentType(), request.getGateway());

        // 1. Validate user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserException(String.format("User %s not found", request.getUserId())));

        // 2. Create payment record
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setGateway(request.getGateway());
        payment.setPaymentType(request.getPaymentType());
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        payment.setDescription(request.getDescription());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId("TXN_" + UUID.randomUUID());
        payment.setInitiatedAt(LocalDateTime.now());

        // 3. Link associations (if provided)
        if(request.getSubscriptionId() != null){
            Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                    .orElseThrow(() -> new SubscriptionException(String.format("Subscription %s not found", request.getSubscriptionId())));
            payment.setSubscription(subscription);

            // Always use the subscription price
            payment.setAmount(subscription.getPlan().getPrice());
        }
        if (request.getBookLoanId() != null) {
            BookLoan loan = bookLoanRepository.findById(request.getBookLoanId())
                    .orElseThrow(() -> new PaymentException("Book loan not found"));
            payment.setBookLoan(loan);
        }

        if(request.getFineId() != null){
            Fine fine = fineRepository.findById(request.getFineId())
                    .orElseThrow(() -> new PaymentException("Fine not found"));
            payment.setFine(fine);
            payment.setAmount(fine.getAmount());
        }


        paymentRepository.save(payment);

        // 4. Initiate payment gateway
        PaymentInitiateResponse response;
        if(request.getGateway() == PaymentGateway.STRIPE){
            PaymentLinkResponse linkResponse = stripeService.createPaymentLink(user, payment);

            response = PaymentInitiateResponse.builder()
                    .paymentId(payment.getId())
                    .gateway(payment.getGateway())
                    .checkoutUrl(linkResponse.getPayment_link_url())
                    .transactionId(linkResponse.getPayment_link_id())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency() != null ? request.getCurrency() : "USD")
                    .description(payment.getDescription())
                    .success(true)
                    .message("Successfully initiated payment for user: " + user.getUsername())
                    .build();
            payment.setGatewayOrderId(linkResponse.getPayment_link_id());
        } else{
            throw new PaymentException("Unsupported Payment Gateway: " + payment.getGateway());
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);


        // Publish payment initiated event
        publishPaymentInitiatedEvent(payment, response.getCheckoutUrl());
        return response;
    }

    @Override
    public PaymentDTO verifyPayment(PaymentVerifyRequest request) {
        Payment payment = paymentRepository.findByGatewayOrderId(request.getStripePaymentIntentId())
                .orElseThrow(() -> new PaymentException(String.format("Session %s not found for session", request.getStripePaymentIntentId())));

        // 2. Idempotency check — avoid reprocessing an already-completed payment
        if(payment.getStatus() == PaymentStatus.SUCCESS){
            log.warn("Payment already completed: {}", payment.getId());
            return paymentMapper.toDto(payment);
        }

        // 3. Verify against Stripe directly (never trust client-sent status)
        boolean isValid = stripeService.isValidPayment(payment.getId());

        // 4. Update status based on verification result
        if(isValid){
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setCompletedAt(LocalDateTime.now());
            log.info("Payment verified successfully: {}", payment.getId());

            paymentRepository.save(payment);

            publishPaymentSuccessEvent(payment);
        }else{
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment Verification Failed");

            paymentRepository.save(payment);

            publishPaymentFailedEvent(payment);
        }
        return paymentMapper.toDto(payment);
    }

    @Override
    public PaymentDTO getPaymentById(Long paymentId) {
        Payment payment =  paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with id: " +  paymentId));
        return  paymentMapper.toDto(payment);
    }

    @Override
    public PaymentDTO getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentException("User haven't had transaction yet."));
        return paymentMapper.toDto(payment);
    }

    @Override
    public Page<PaymentDTO> getAllPayments(Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(paymentMapper::toDto);
    }

    @Override
    public Page<PaymentDTO> getUserPayments(Long userId, Pageable pageable) {
        if(!userRepository.existsById(userId)){
            throw new UserException(String.format("User %s not found", userId));
        }
        Page<Payment> payments = paymentRepository.findByUserIdAndActiveTrue(userId, pageable);

        return payments.map(paymentMapper::toDto);
    }

    @Override
    public PaymentDTO cancelPayment(Long paymentId) {
        Payment payment =  paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with id: " +  paymentId));
        if(!payment.getStatus().equals(PaymentStatus.PENDING)){
            throw new PaymentException("Only pending payments can be cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);
        log.info("Payment cancelled: {}", paymentId);

        return paymentMapper.toDto(payment);
    }

    @Override
    public PaymentInitiateResponse retryPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with id: " +  paymentId));

        if(!payment.canRetry()){
            throw new PaymentException("Payment cannot be retried; Max retry attempts reached or payment is not in failed/cancelled state.");
        }

        PaymentInitiateRequest request = new PaymentInitiateRequest();
        request.setUserId(payment.getUser().getId());
        request.setSubscriptionId(payment.getSubscription().getId() != null ? payment.getSubscription().getId() : null);
        request.setPaymentType(payment.getPaymentType());
        request.setGateway(payment.getGateway());
        request.setCurrency(payment.getCurrency() != null ? request.getCurrency() : "USD");
        request.setDescription(payment.getDescription());

        payment.setRetryCount(payment.getRetryCount() + 1);
        paymentRepository.save(payment);
        return initiatePayment(request);
    }

    @Override
    public RevenueStatisticResponse getMonthlyRevenue() {
        List<Payment> payments = paymentRepository.findAll();

        int currentDay = LocalDateTime.now().getDayOfMonth();
        int currentYear = LocalDateTime.now().getYear();
        int currentMonth = LocalDateTime.now().getMonthValue();
        // filter only successful payment of this month
        double totalRevenue = payments.stream()
                .filter(Payment::isSuccess)
                .filter(payment -> payment.getCreatedAt() != null &&
                        payment.getCreatedAt().getYear() == currentYear &&
                        payment.getCreatedAt().getMonthValue() == currentMonth &&
                        payment.getCreatedAt().getDayOfMonth() == currentDay)
                .mapToDouble(Payment::getAmount)
                .sum();
        String currency = payments.stream()
                .filter(Payment::isSuccess)
                .map(Payment::getCurrency)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("USD");
        RevenueStatisticResponse response = new RevenueStatisticResponse();
        response.setMonthlyRevenue(totalRevenue);
        response.setYear(currentYear);
        response.setMonthlyRevenue(currentMonth);
        response.setDay(currentDay);
        response.setCurrency(currency);
        return response;
    }


    /**
     * Publish payment initiated event to notify other services.
     * This can be used for tracking and sending initial notifications.
     *
     * @param payment The initiated payment
     * @param checkoutUrl The URL for user to complete payment
     */

    private void publishPaymentInitiatedEvent(Payment payment, String checkoutUrl) {
        PaymentInitiatedEvent event = PaymentInitiatedEvent.builder()
                .paymentId(payment.getId())
                .userId(payment.getUser().getId())
                .paymentType(payment.getPaymentType())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .subscriptionId(payment.getSubscription() != null ? payment.getSubscription().getId() : null)
                .transactionId(payment.getTransactionId())
                .initiatedAt(payment.getInitiatedAt())
                .description(payment.getDescription())
                .userEmail(payment.getUser().getEmail())
                .userName(payment.getUser().getUsername())
                .checkoutUrl(checkoutUrl)
                .build();
    }

    /**
     * Publish payment success event to notify other services.
     * This decouples payment processing from domain-specific actions.
     *
     * @param payment The successful payment
     */

    private void publishPaymentSuccessEvent(Payment payment){
        PaymentSuccessEvent event = PaymentSuccessEvent.builder()
                .paymentId(payment.getId())
                .userId(payment.getUser().getId())
                .type(payment.getPaymentType())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .subscriptionId(payment.getSubscription() != null ? payment.getSubscription().getId() : null)
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .transactionId(payment.getTransactionId())
                .completedAt(payment.getCompletedAt())
                .description(payment.getDescription())
                .build();
    }

    /**
     * Publish payment failed event to notify other services.
     * This allows services to react to failures (e.g., send notifications, log errors).
     *
     * @param payment The failed payment
     */
    private void publishPaymentFailedEvent(Payment payment){
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .paymentId(payment.getId())
                .userId(payment.getUser().getId())
                .failureReason(payment.getFailureReason())
                .paymentType(payment.getPaymentType())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .subscriptionId(payment.getSubscription() != null ? payment.getSubscription().getId() : null)
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .description(payment.getDescription())
                .userEmail(payment.getUser().getEmail())
                .userName(payment.getUser().getUsername())
                .failedAt(payment.getFailedAt())
                .build();
    }
}
