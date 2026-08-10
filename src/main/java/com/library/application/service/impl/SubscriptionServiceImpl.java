package com.library.application.service.impl;

import com.library.application.domain.PaymentStatus;
import com.library.application.domain.PaymentType;
import com.library.application.entity.Payment;
import com.library.application.entity.Subscription;
import com.library.application.entity.SubscriptionPlan;
import com.library.application.entity.User;
import com.library.application.exception.SubscriptionException;
import com.library.application.exception.SubscriptionPlanException;
import com.library.application.mapper.SubscriptionMapper;
import com.library.application.payload.dto.SubscriptionDTO;
import com.library.application.payload.request.PaymentInitiateRequest;
import com.library.application.payload.request.SubscriptionRequest;
import com.library.application.payload.response.PaymentInitiateResponse;
import com.library.application.repository.PaymentRepository;
import com.library.application.repository.SubscriptionPlanRepository;
import com.library.application.repository.SubscriptionRepository;
import com.library.application.repository.UserRepository;
import com.library.application.service.PaymentService;
import com.library.application.service.SubscriptionService;
import com.library.application.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;


    @Override
    @Transactional
    public SubscriptionDTO createSubscription(SubscriptionRequest subscriptionDTO) {
        log.info("Processing subscription request for user : {}, plan ID: {}", subscriptionDTO.getUserId(), subscriptionDTO.getPlanId());

        // Get Current User
        User user = getAuthenticatedUser();

        // Get the subscription plan
        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscriptionDTO.getPlanId())
                .orElseThrow(() -> new SubscriptionPlanException("Subscription Plan Not Found"));

        // Validate plan is active
        if(!plan.getIsActive()){
            throw new SubscriptionException("Subscription plan is not currently available: " + subscriptionDTO.getPlanId());
        }

        // Check if user already has an active subscription
        Optional<Subscription> existingSubscription =subscriptionRepository.findActiveSubscriptionByUserId(user.getId(), LocalDate.now());
        if(existingSubscription.isPresent()){
            throw new SubscriptionException("User already subscribed this plan. Please cancel it before subscribing to a new plan.");
        }

        Subscription subscription = new  Subscription();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setAutoRenew(subscriptionDTO.getAutoRenew() != null ? subscriptionDTO.getAutoRenew() : false);
        // Initialize from plan(sets price, maxBooks, maxDays, dates)
        subscription.initializeFromPlan();

        // Subscription starts as inactive until payment is confirmed
        subscription.setIsActive(false);

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toDto(savedSubscription);
    }

    @Override
    @Transactional
    public PaymentInitiateResponse createSubscriptionWithPayment(SubscriptionRequest subscriptionDTO) {
        log.info("Processing payment subscription request for user : {}, plan ID: {}", subscriptionDTO.getUserId(), subscriptionDTO.getPlanId());

        // Get Current User
        User user = getAuthenticatedUser();

        // Get the subscription plan
        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscriptionDTO.getPlanId())
                .orElseThrow(() -> new SubscriptionPlanException("Subscription Plan Not Found"));

        // Validate plan is active
        if(!plan.getIsActive()){
            throw new SubscriptionException("Subscription plan is not currently available: " + subscriptionDTO.getPlanId());
        }

        // Check if user already has an active subscription
        Optional<Subscription> existingSubscription =subscriptionRepository.findActiveSubscriptionByUserId(user.getId(), LocalDate.now());
        if(existingSubscription.isPresent()){
            throw new SubscriptionException("User already subscribed this plan. Please cancel it before subscribing to a new plan.");
        }

        Subscription subscription = new  Subscription();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setAutoRenew(subscriptionDTO.getAutoRenew() != null ? subscriptionDTO.getAutoRenew() : false);
        // Initialize from plan(sets price, maxBooks, maxDays, dates)
        subscription.initializeFromPlan();

        // Subscription starts as inactive until payment is confirmed
        subscription.setIsActive(false);

        subscriptionRepository.save(subscription);

        // Create payment entity
        PaymentInitiateRequest  paymentInitiateRequest = PaymentInitiateRequest.builder()
                .userId(user.getId())
                .subscriptionId(subscription.getId())
                .paymentType(PaymentType.MEMBERSHIP)
                .gateway(subscriptionDTO.getPaymentGateway())
                .successUrl(subscriptionDTO.getSuccessUrl())
                .cancelUrl(subscriptionDTO.getCancelUrl())
                .description("Library Subscription - " + plan.getName())
                .build();

        return paymentService.initiatePayment(paymentInitiateRequest);
    }

    @Override
    public SubscriptionDTO getUserActiveSubscription(Long userId) {
        if(userId != null){
            if(!userRepository.existsById(userId)){
                throw new UsernameNotFoundException("User not found");
            }else {
                User user = userService.getCurrentUser();
                userId = user.getId();
            }
        }

        Subscription subscription = subscriptionRepository
                .findActiveSubscriptionByUserId(userId, LocalDate.now())
                .orElseThrow(() -> new SubscriptionException("No active subscription for this user"));

        return subscriptionMapper.toDto(subscription);
    }

    @Override
    public SubscriptionDTO getSubscriptionById(Long subscriptionId) {
        return subscriptionMapper.toDto(subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionException("Subscription Not Found")));
    }

    @Override
    public SubscriptionDTO activateSubscription(Long subscriptionId, Long paymentId) {
        log.info("Activating subscription: {}", subscriptionId);
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionException("Subscription Not Found"));

        Payment payment  = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new SubscriptionException("Payment Not Found"));

        // Verify payment is successful
        if(payment.getStatus() != PaymentStatus.SUCCESS){
            throw new SubscriptionException("Cannot activate subscription. Payment status is " + payment.getStatus());
        }

        // Verify payment belongs to this subscription id
        if(!payment.getSubscription().getId().equals(subscriptionId)){
            throw new SubscriptionException("Cannot activate subscription. Payment subscription ID is " + subscription.getId());
        }

        // Active Subscription
        subscription.setIsActive(true);

        // Ensure start data is set
        if(subscription.getStartDate() == null || subscription.getStartDate().isBefore(LocalDate.now())){
            subscription.setStartDate(LocalDate.now());
            subscription.calculateEndDate();
        }
        Subscription activatedSubscription = subscriptionRepository.save(subscription);

        log.info("Subscription activated successfully: {}", subscriptionId);
        return subscriptionMapper.toDto(activatedSubscription);
    }

    @Override
    public SubscriptionDTO cancelSubscription(Long subscriptionId, String reason) {
        log.info("Cancelling subscription: {} with reason: {}", subscriptionId, reason);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionException("Subscription Not Found"));

        if(!subscription.getIsActive()){
            throw new SubscriptionException("This subscription has been already inactive");
        }

        subscription.setIsActive(false);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setNotes(subscription.getNotes() == null ? reason : "Cancelled By User");

        log.info("Cancelling subscription: {} successfully", subscriptionId);
        return subscriptionMapper.toDto(subscriptionRepository.save(subscription));
    }



    @Override
    public List<SubscriptionDTO> getUserSubscriptions(Long userId) {
        if(userId != null){
            if(!userRepository.existsById(userId)){
                throw new UsernameNotFoundException("User not found");
            }else {
                User user = userService.getCurrentUser();
                userId = user.getId();
            }
        }
        List<Subscription> subscriptions = subscriptionRepository
                .findByUserIdOrderByCreatedAtDesc(userId);

        return subscriptions.stream()
                .map(subscriptionMapper::toDto)
                .toList();
    }

    @Override
    public PaymentInitiateResponse renewSubscription(Long planId, SubscriptionRequest request) {
        Subscription oldSubscription = subscriptionRepository.findById(planId)
                .orElseThrow(() -> new SubscriptionException("Subscription Not Found"));

        if(oldSubscription.getIsActive()){
            oldSubscription.setIsActive(false);
            oldSubscription.setCancelledAt(LocalDateTime.now());
            oldSubscription.setCancellationReason("Renewed to new subscription");
            subscriptionRepository.save(oldSubscription);
        }

        request.setUserId(oldSubscription.getUser().getId());
        if(request.getUserId() == null){
            request.setPlanId(oldSubscription.getPlan().getId());
        }
        return createSubscriptionWithPayment(request);
    }


    @Override
    public List<SubscriptionDTO> getAllSubscriptions(Pageable pageable) {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        if(subscriptions.isEmpty()){
            throw new SubscriptionException("No subscriptions found");
        }
        return subscriptions.stream()
                .map(subscriptionMapper::toDto)
                .toList();
    }

    @Override
    public void deactivateExpiredSubscription() {
        log.info("Running subscription expiry check at {}", LocalDateTime.now());

        List<Subscription> subscriptions = subscriptionRepository.findExpiredActiveSubscriptions(LocalDate.now());
        if(subscriptions.isEmpty()){
            throw new SubscriptionException("No subscriptions found");
        }

        int deactivatedCount = 0;
        for(Subscription subscription : subscriptions){
            subscription.setIsActive(false);
            subscription.setNotes((subscription.getNotes() != null ? subscription.getNotes() + "\n": "")
                    + "Auto-deactivated on " + LocalDateTime.now() + " due to expired subscription");
            subscriptionRepository.save(subscription);
            deactivatedCount++;
            log.debug("Deactivated expired subscription ID: {} for user: {}", subscription.getId(), subscription.getUser().getId());
        }

        log.info("Deactivated {} expired subscriptions", deactivatedCount);

    }


    // Using this method to find the user has subscribed
    @Override
    public boolean hasValidSubscription(Long userId) {
        return subscriptionRepository.hasActiveSubscriptionByUserId(userId, LocalDate.now());
    }


    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("User not authenticated");
        }
        String email = authentication.getName();
        return  userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Authentication user not found"));
    }
}
