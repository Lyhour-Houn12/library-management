package com.library.application.service.impl;

import com.library.application.entity.SubscriptionPlan;
import com.library.application.exception.SubscriptionPlanException;
import com.library.application.mapper.SubscriptionPlanMapper;
import com.library.application.payload.dto.SubscriptionPlanDTO;
import com.library.application.payload.request.SubscriptionPlanFilter;
import com.library.application.payload.response.PageResponse;
import com.library.application.repository.SubscriptionPlanRepository;
import com.library.application.service.SubscriptionPlanService;
import com.library.application.service.UserService;
import com.library.application.specification.SubscriptionSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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

        // Set audit fields
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
    public SubscriptionPlanDTO activatePlan(Long planId) {
        log.info("Activating subscription plan {}", planId);
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new SubscriptionPlanException(String.format("Subscription plan ID %s not found", planId)));

        plan.setIsActive(true);
        plan.setUpdatedBy(userService.getCurrentUserEmail());
        subscriptionPlanRepository.save(plan);
        return subscriptionPlanMapper.toDto(plan);
    }

    @Override
    public SubscriptionPlanDTO deactivatePlan(Long planId) {
        log.info("Deactivating subscription plan {}", planId);
        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findById(planId)
                        .orElseThrow(() -> new SubscriptionPlanException(String.format("Subscription plan ID %s not found", planId)));

        subscriptionPlan.setIsActive(false);
        subscriptionPlan.setUpdatedBy(userService.getCurrentUserEmail());
        subscriptionPlanRepository.save(subscriptionPlan);

        return subscriptionPlanMapper.toDto(subscriptionPlan);
    }


    @Override
    public SubscriptionPlan getSubscriptionPlanByCode(String planCode) {
        return subscriptionPlanRepository.findByPlanCode(planCode)
                .orElseThrow(() -> new SubscriptionPlanException(String.format("Subscription plan code does not find with this code %s", planCode)));
    }

    @Override
    public SubscriptionPlanDTO getSubscriptionPlanById(Long planId) {
        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new SubscriptionPlanException(String.format("Subscription plan ID %s not found", planId)));
        return subscriptionPlanMapper.toDto(subscriptionPlan);
    }

    @Override
    public List<SubscriptionPlanDTO> findAllSubscriptionPlan() {
        return List.of();
    }

    @Override
    public Page<SubscriptionPlanDTO> getAllPlans(Pageable pageable) {
        Page<SubscriptionPlan> plans = subscriptionPlanRepository.findAllPlansOrdered(pageable);
        return plans.map(subscriptionPlanMapper::toDto);
    }

    @Override
    public Page<SubscriptionPlanDTO> getAllActivePlans(Pageable pageable) {
        Page<SubscriptionPlan> plans = subscriptionPlanRepository.findAllActivePlans(pageable);
        return plans.map(subscriptionPlanMapper::toDto);
    }

    @Override
    public List<SubscriptionPlanDTO> getFeaturedPlans() {
        return subscriptionPlanRepository.findAll()
                .stream()
                .filter(subscriptionPlan -> subscriptionPlan.getIsActive() == true)
                .map(subscriptionPlanMapper::toDto)
                .toList();
    }

    @Override
    public PageResponse<SubscriptionPlanDTO> searchPlans(SubscriptionPlanFilter filter, Pageable pageable) {
        if(filter == null){
            filter = new SubscriptionPlanFilter();
        }
        Specification<SubscriptionPlan> spec = SubscriptionSpecification.subscriptionPlanFilter(filter);
        Sort sort = Sort.by(filter.getDirection(), filter.getSortBy().getFieldName())
                .and(Sort.by(Sort.Direction.ASC, "id"));
        Pageable page = PageRequest.of(pageable.getPageNumber() , pageable.getPageSize(), sort);
        //Page<SubscriptionPlanDTO> plans = subscriptionPlanRepository.findAll(spec, pageable, sort).map(subscriptionPlanMapper::toDto);
        return PageResponse.from(
                subscriptionPlanRepository.findAll(spec, page)
                        .map(subscriptionPlanMapper::toDto)
        );
    }


    @Override
    public List<SubscriptionPlanDTO> getPlansByCurrency(String currency) {
        SubscriptionPlanFilter filter = new SubscriptionPlanFilter();
        filter.setActive(true);
        filter.setCurrency(currency);
        Specification<SubscriptionPlan> spec = SubscriptionSpecification.subscriptionPlanFilter(filter);

        return subscriptionPlanRepository.findAll(spec)
                .stream()
                .map(subscriptionPlanMapper::toDto)
                .toList();
    }

    @Override
    public boolean planCodeExists(String planCode) {
        return subscriptionPlanRepository.existsByPlanCode(planCode);
    }




}
