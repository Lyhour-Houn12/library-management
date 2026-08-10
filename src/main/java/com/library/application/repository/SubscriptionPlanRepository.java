package com.library.application.repository;

import com.library.application.entity.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long>, JpaSpecificationExecutor<SubscriptionPlan> {

    boolean existsByPlanCode(String planCode);

    Optional<SubscriptionPlan> findByPlanCode(String planCode);

    @Query("SELECT sp FROM SubscriptionPlan sp ORDER BY sp.displayOrder ASC, sp.durationDays ASC")
    Page<SubscriptionPlan> findAllPlansOrdered(Pageable pageable);

    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.isActive = TRUE ORDER BY sp.displayOrder ASC, sp.durationDays ASC")
    Page<SubscriptionPlan> findAllActivePlans(Pageable pageable);
}
