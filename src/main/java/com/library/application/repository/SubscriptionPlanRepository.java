package com.library.application.repository;

import com.library.application.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    boolean existsByPlanCode(String planCode);

    Optional<SubscriptionPlan> findByPlanCode(String planCode);
}
