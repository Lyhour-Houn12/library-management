package com.library.application.mapper;

import com.library.application.entity.Payment;
import com.library.application.entity.Subscription;
import com.library.application.entity.User;
import com.library.application.exception.PaymentException;
import com.library.application.payload.dto.PaymentDTO;
import com.library.application.repository.PaymentRepository;
import com.library.application.repository.SubscriptionRepository;
import com.library.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentMapper {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public PaymentDTO toDto(Payment payment) {
        if (payment == null) {
            return null;
        }
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());

        // User information
        if(payment.getUser() != null) {
            dto.setUserId(payment.getUser().getId());
            dto.setUsername(payment.getUser().getUsername());
            dto.setUserEmail(payment.getUser().getEmail());
        }
        // Subscription information
        if(payment.getSubscription() != null) {
            dto.setSubscriptionId(payment.getSubscription().getId());
        }
        dto.setPaymentType(payment.getPaymentType());
        dto.setStatus(payment.getStatus());
        dto.setGateway(payment.getGateway());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setTransactionId(payment.getTransactionId());
        dto.setGatewayPaymentId(payment.getGatewayPaymentId());
        dto.setGatewayOrderId(payment.getGatewayOrderId());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setDescription(payment.getDescription());
        dto.setFailureReason(payment.getFailureReason());
        dto.setRetryCount(payment.getRetryCount());
        dto.setInitiatedAt(payment.getInitiatedAt());
        dto.setCompletedAt(payment.getCompletedAt());
        dto.setNotificationSent(payment.getNotificationSent());
        dto.setActive(payment.getActive());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }

    Payment toEntity(PaymentDTO dto) {
        if (dto == null) {
            return null;
        }

        Payment payment = new Payment();
        payment.setId(dto.getId());

        // Map user
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new PaymentException("User with ID " + dto.getUserId() + " not found"));
            payment.setUser(user);
        }
        // Map subscription if provided
        if (dto.getSubscriptionId() != null) {
            Subscription subscription = subscriptionRepository.findById(dto.getSubscriptionId())
                    .orElseThrow(() -> new PaymentException("Subscription with ID " + dto.getSubscriptionId() + " not found"));
            payment.setSubscription(subscription);
        }

        payment.setPaymentType(dto.getPaymentType());
        payment.setStatus(dto.getStatus());
        payment.setGateway(dto.getGateway());
        payment.setAmount(dto.getAmount());
        payment.setCurrency(dto.getCurrency());
        payment.setTransactionId(dto.getTransactionId());
        payment.setGatewayPaymentId(dto.getGatewayPaymentId());
        payment.setGatewayOrderId(dto.getGatewayOrderId());
        payment.setGatewaySignature(dto.getGatewaySignature());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setDescription(dto.getDescription());
        payment.setFailureReason(dto.getFailureReason());
        payment.setRetryCount(dto.getRetryCount() != null ? dto.getRetryCount() : 0);
        payment.setInitiatedAt(dto.getInitiatedAt());
        payment.setCompletedAt(dto.getCompletedAt());
        payment.setNotificationSent(dto.getNotificationSent() != null ? dto.getNotificationSent() : false);
        payment.setActive(dto.getActive() != null ? dto.getActive() : true);

        return payment;

    }

    public List<PaymentDTO> toDtoList(List<Payment> payments) {
        if(payments == null) return null;
        return payments.stream()
                .map(this::toDto)
                .toList();
    }
}
