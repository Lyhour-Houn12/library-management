package com.library.application.service;

import com.library.application.entity.SubscriptionPlan;
import com.library.application.payload.dto.SubscriptionPlanDTO;

import java.util.List;

public interface SubscriptionPlanService {

    SubscriptionPlanDTO createSubscriptionPlan(SubscriptionPlanDTO planDTO);

    SubscriptionPlanDTO updateSubscriptionPlan(Long planId,SubscriptionPlanDTO planDTO);

    void deletePlan(Long planId);

    List<SubscriptionPlanDTO> findAllSubscriptionPlan();

    SubscriptionPlan getSubscriptionPlanByCode(String planCode);

    SubscriptionPlanDTO activatePlan(Long planId);
}
