package com.library.application.controller;

import com.library.application.payload.dto.SubscriptionPlanDTO;
import com.library.application.payload.response.ApiResponse;
import com.library.application.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subcription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {
    private final SubscriptionPlanService subscriptionPlanService;


    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSubscriptionPlan(@Valid @RequestBody SubscriptionPlanDTO planDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionPlanService.createSubscriptionPlan(planDto));
    }

    @PutMapping("/admin/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSubscriptionPlan(@PathVariable Long planId ,@Valid @RequestBody SubscriptionPlanDTO planDto) {
        return ResponseEntity.status(HttpStatus.OK).body(subscriptionPlanService.updateSubscriptionPlan(planId,planDto));
    }

    @DeleteMapping("/admin/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSubscriptionPlan(@PathVariable Long planId) {
        subscriptionPlanService.deletePlan(planId);
        return ResponseEntity.ok(new ApiResponse("Subscription Plan has been deleted successfully", true));
    }


    @PostMapping("/admin/{planId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activatePlan(@PathVariable Long planId) {
        SubscriptionPlanDTO plan = subscriptionPlanService.activatePlan(planId);
        return ResponseEntity.ok(plan);

    }

    @GetMapping("/code/{planCode}")
    public ResponseEntity<?> getSubscriptionPlanCode(@PathVariable String planCode) {
        return ResponseEntity.ok(subscriptionPlanService.getSubscriptionPlanByCode(planCode));
    }




}
