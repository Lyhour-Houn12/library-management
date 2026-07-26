package com.library.application.mapper;

import com.library.application.entity.Subscription;
import com.library.application.entity.SubscriptionPlan;
import com.library.application.entity.User;
import com.library.application.exception.SubscriptionPlanException;
import com.library.application.exception.UserException;
import com.library.application.payload.dto.SubscriptionDTO;
import com.library.application.repository.SubscriptionPlanRepository;
import com.library.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscriptionMapper {
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public SubscriptionDTO toDto(Subscription subscription) {
        if(subscription == null) {
            return null;
        }
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setId(subscription.getId());

        // user information
        if(subscription.getUser() != null){
            User user = userRepository.findById(subscription.getUser().getId())
                    .orElseThrow(() -> new UserException("User not found"));
            dto.setUserId(user.getId());
            dto.setUserName(user.getUsername());
            dto.setUserEmail(user.getEmail());
        }

        // plan information
        if(subscription.getPlan() != null){
            dto.setPlanId(subscription.getPlan().getId());
        }

        dto.setPlanName(subscription.getPlanName());
        dto.setPlanCode(subscription.getPlanCode());
        dto.setPrice(subscription.getPrice());
        dto.setCurrency(subscription.getCurrency());
        dto.setStartDate(subscription.getStartDate());
        dto.setEndDate(subscription.getEndDate());
        dto.setIsActive(subscription.getIsActive());
        dto.setMaxBooksAllowed(subscription.getMaxBooksAllowed());
        dto.setMaxDaysPerBook(subscription.getMaxDaysPerBook());
        dto.setAutoRenew(subscription.getAutoRenew());
        dto.setCancelledAt(subscription.getCancelledAt());
        dto.setCancellationReason(subscription.getCancellationReason());
        dto.setNotes(subscription.getNotes());
        dto.setCreatedAt(subscription.getCreatedAt());
        dto.setUpdatedAt(subscription.getUpdatedAt());

        // Calculated fields
        dto.setDaysRemaining(subscription.getRemainingDays());
        dto.setIsValid(subscription.isValid());
        dto.setIsExpired(subscription.isExpired());

        return dto;
    }

    public Subscription toEntity(SubscriptionDTO subscriptionDTO) {
        if(subscriptionDTO == null) {
            return null;
        }
        Subscription subscription = new Subscription();
        subscription.setPrice(subscriptionDTO.getPrice());

        // Map user
        if(subscriptionDTO.getUserId() != null) {
            User user = userRepository.findById(subscriptionDTO.getUserId())
                    .orElseThrow(() -> new UserException("User not found"));
            subscription.setUser(user);
        }
        // Map plan
        if(subscriptionDTO.getPlanId() != null) {
            SubscriptionPlan plan = subscriptionPlanRepository.findById(subscriptionDTO.getPlanId())
                    .orElseThrow(() -> new SubscriptionPlanException("Plan not found"));
            subscription.setPlan(plan);
        }
        subscription.setPlanName(subscriptionDTO.getPlanName());
        subscription.setPlanCode(subscriptionDTO.getPlanCode());
        subscription.setPrice(subscriptionDTO.getPrice());
        subscription.setCurrency(subscriptionDTO.getCurrency());
        subscription.setStartDate(subscriptionDTO.getStartDate());
        subscription.setEndDate(subscriptionDTO.getEndDate());
        subscription.setIsActive(subscriptionDTO.getIsActive() != null ? subscriptionDTO.getIsActive() : false);
        subscription.setMaxBooksAllowed(subscriptionDTO.getMaxBooksAllowed());
        subscription.setMaxDaysPerBook(subscriptionDTO.getMaxDaysPerBook());
        subscription.setAutoRenew(subscriptionDTO.getAutoRenew() != null ? subscriptionDTO.getAutoRenew() : false);
        subscription.setCancelledAt(subscriptionDTO.getCancelledAt());
        subscription.setCancellationReason(subscriptionDTO.getCancellationReason());
        subscription.setNotes(subscriptionDTO.getNotes());

        return subscription;

    }

    public List<SubscriptionDTO> toDtoList(List<Subscription> subscriptions){
        if(subscriptions == null || subscriptions.isEmpty()){
            return null;
        }
        return subscriptions.stream()
                .map(this::toDto)
                .toList();
    }
}
