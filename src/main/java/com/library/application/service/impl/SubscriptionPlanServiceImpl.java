package com.library.application.service.impl;

import com.library.application.entity.SubscriptionPlan;
import com.library.application.exception.SubscriptionPlanException;
import com.library.application.mapper.SubscriptionPlanMapper;
import com.library.application.payload.dto.SubscriptionPlanDTO;
import com.library.application.repository.SubscriptionPlanRepository;
import com.library.application.service.SubscriptionPlanService;
import com.library.application.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;
    private final UserService userService;

    @Override
    public SubscriptionPlanDTO createSubscriptionPlan(SubscriptionPlanDTO planDTO) {
        log.info("Creating subscription plan {}", planDTO.getPlanCode());

        if(subscriptionPlanRepository.existsByPlanCode(planDTO.getPlanCode())) {
            throw new SubscriptionPlanException("Plan code already exists");
        }

        SubscriptionPlan  subscriptionPlan = subscriptionPlanMapper.toEntity(planDTO);

        String currentUser = userService.getCurrentUserEmail();
        subscriptionPlan.setCreatedBy(currentUser);
        subscriptionPlan.setUpdatedBy(currentUser);

        subscriptionPlan = subscriptionPlanRepository.save(subscriptionPlan);
        log.info("Created subscription plan {}", subscriptionPlan.getPlanCode());

        return subscriptionPlanMapper.toDto(subscriptionPlan);
    }

    @Override
    public SubscriptionPlanDTO updateSubscriptionPlan(Long planId, SubscriptionPlanDTO planDTO) {
        log.info("Updating subscription plan ID: {}", planId);
        SubscriptionPlan existingPlan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new SubscriptionPlanException(String.format("Subscription plan ID %s not found", planId)));

        // Not allowing change plan code
        if(planDTO.getPlanCode() != null && !planDTO.getPlanCode().equals(existingPlan.getPlanCode()) ) {
            throw new SubscriptionPlanException("Plan code can not be changed after creation");
        }

        subscriptionPlanMapper.updateEntity(planDTO, existingPlan);

        // update by audit
        existingPlan.setUpdatedBy(userService.getCurrentUserEmail());

        subscriptionPlanRepository.save(existingPlan);

        log.info("Updated subscription plan {}", existingPlan.getPlanCode());
        return subscriptionPlanMapper.toDto(existingPlan);
    }

    @Override
    public void deletePlan(Long planId) {
        log.info("Deleting subscription plan ID: {}", planId);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new SubscriptionPlanException(String.format("Subscription plan ID %s not found", planId)));

        plan.setIsActive(false);

        plan.setUpdatedBy(userService.getCurrentUserEmail());
        subscriptionPlanRepository.save(plan);
        log.info("Deactivated subscription plan {}", plan.getPlanCode());
    }

    @Override
    public List<SubscriptionPlanDTO> findAllSubscriptionPlan() {
        return List.of();
    }

    @Override
    public SubscriptionPlan getSubscriptionPlanByCode(String planCode) {
        return subscriptionPlanRepository.findByPlanCode(planCode).orElseThrow(() -> new SubscriptionPlanException(String.format("Subscription plan code does not find with this code %s", planCode)));
    }

    @Override
    public SubscriptionPlanDTO activatePlan(Long planId) {
        log.info("Activating subscription plan {}", planId);
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new SubscriptionPlanException(String.format("Subscription plan ID %s not found", planId)));

        plan.setIsActive(true);
        plan.setUpdatedBy(userService.getCurrentUserEmail());
        subscriptionPlanRepository.save(plan);
        return subscriptionPlanMapper.toDto(plan);
    }
}
