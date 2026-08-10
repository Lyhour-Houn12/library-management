package com.library.application.service;

import com.library.application.entity.SubscriptionPlan;
import com.library.application.payload.dto.SubscriptionPlanDTO;
import com.library.application.payload.request.SubscriptionPlanFilter;
import com.library.application.payload.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SubscriptionPlanService {

    SubscriptionPlanDTO createSubscriptionPlan(SubscriptionPlanDTO planDTO);

    SubscriptionPlanDTO updateSubscriptionPlan(Long planId,SubscriptionPlanDTO planDTO);

    void deletePlan(Long planId);

    SubscriptionPlanDTO activatePlan(Long planId);

    SubscriptionPlan getSubscriptionPlanByCode(String planCode);

    SubscriptionPlanDTO deactivatePlan(Long planId);

    SubscriptionPlanDTO getSubscriptionPlanById(Long planId);

    List<SubscriptionPlanDTO> findAllSubscriptionPlan();

    Page<SubscriptionPlanDTO> getAllPlans(Pageable pageable);

    Page<SubscriptionPlanDTO> getAllActivePlans(Pageable pageable);

    List<SubscriptionPlanDTO> getFeaturedPlans();

    /**
     * Search plans by name or description
     */
    PageResponse<SubscriptionPlanDTO> searchPlans(SubscriptionPlanFilter filter, Pageable pageable);

    /**
     * Get plans by currency
     */
    List<SubscriptionPlanDTO> getPlansByCurrency(String currency);

    /**
     * Check if plan code exists
     */
    boolean planCodeExists(String planCode);

}
