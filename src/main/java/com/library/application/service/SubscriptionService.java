package com.library.application.service;

import com.library.application.payload.dto.SubscriptionDTO;
import com.library.application.payload.request.SubscriptionRequest;
import com.library.application.payload.response.PaymentInitiateResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SubscriptionService {

    SubscriptionDTO createSubscription(SubscriptionRequest request);

    PaymentInitiateResponse createSubscriptionWithPayment(SubscriptionRequest request);

    SubscriptionDTO getUserActiveSubscription(Long userId);

    List<SubscriptionDTO> getUserSubscriptions(Long userId);

    PaymentInitiateResponse renewSubscription(Long planId, SubscriptionRequest request);

    SubscriptionDTO getSubscriptionById(Long subscriptionId);

    SubscriptionDTO activateSubscription(Long subscriptionId, Long paymentId);

    SubscriptionDTO cancelSubscription(Long subscriptionId, String reason);
    /*
        This method is for admin
     */
    List<SubscriptionDTO> getAllSubscriptions(Pageable pageable);

    void deactivateExpiredSubscription();  // not using yet will implement new method called scheduler

    /**
     * Check if user has valid subscription
     */
    boolean hasValidSubscription(Long userId);
}
