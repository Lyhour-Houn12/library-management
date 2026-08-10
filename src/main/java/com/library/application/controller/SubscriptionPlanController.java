package com.library.application.controller;

import com.library.application.payload.dto.SubscriptionPlanDTO;
import com.library.application.payload.request.SubscriptionPlanFilter;
import com.library.application.payload.response.ApiResponse;
import com.library.application.payload.response.PageResponse;
import com.library.application.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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


    @PostMapping("/search")
    public ResponseEntity<PageResponse<SubscriptionPlanDTO>> searchSubscriptionPlansByFilter(
            @Valid @RequestBody (required = false) SubscriptionPlanFilter filter,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(subscriptionPlanService.searchPlans(filter, pageable));
    }

    @GetMapping("/currency/{currency}")
    public ResponseEntity<?> getCurrency(@PathVariable String currency) {
        return ResponseEntity.ok(subscriptionPlanService.getPlansByCurrency(currency));
    }


    @GetMapping("/exists/{planCode}")
    public ResponseEntity<?> getSubscriptionPlanExists(@PathVariable String planCode) {
        return new ResponseEntity<>(subscriptionPlanService.planCodeExists(planCode), HttpStatus.OK);
    }

    @PatchMapping("/admin/deactivate/{planId}")
    public ResponseEntity<?> deactivatePlan(@PathVariable Long planId) {
        return new ResponseEntity<>(subscriptionPlanService.deactivatePlan(planId), HttpStatus.OK);
    }





}
