package com.library.application.controller;

import com.library.application.payload.dto.SubscriptionDTO;
import com.library.application.payload.request.SubscriptionRequest;
import com.library.application.payload.response.ApiResponse;
import com.library.application.payload.response.PaymentInitiateResponse;
import com.library.application.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscription(@Valid @RequestBody SubscriptionRequest request) {
        PaymentInitiateResponse subscription = subscriptionService.createSubscriptionWithPayment(request);
        return new ResponseEntity<>(subscription, HttpStatus.CREATED);
    }

    @GetMapping("/user/active")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getActiveSubscriptionFromUser(@RequestParam(value = "userId", required = false) Long userId){
        return ResponseEntity.ok(subscriptionService.getUserActiveSubscription(userId));
    }
    @GetMapping("/user/history")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getMySubscription(@RequestParam(value = "userId", required = false) Long userId){
        return ResponseEntity.ok(subscriptionService.getUserSubscriptions(userId));
    }


    @GetMapping("/check-valid/{userId}")
    public ResponseEntity<?> checkValidSubscription(@PathVariable Long userId){
        Boolean hasValid = subscriptionService.hasValidSubscription(userId);
        return ResponseEntity.ok(new ApiResponse(hasValid ? "User has valid subscription" : "No valid subscription", hasValid));
    }

    @PostMapping("/cancel/{subscriptionId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> cancelSubscription(@PathVariable Long subscriptionId, @RequestParam(name = "reason", required = false) String reason){
        return ResponseEntity.ok(subscriptionService.cancelSubscription(subscriptionId, reason));
    }

    @PostMapping("/active")
    public ResponseEntity<?> activeSubscription(@RequestParam Long subscriptionId){
        return ResponseEntity.ok(subscriptionService.activateSubscription(subscriptionId));
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllSubscriptions(@RequestParam(name = "page", defaultValue = "0", required = false) Integer page,
                                                 @RequestParam(name = "size", defaultValue = "20",required = false) Integer size){
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions(pageable));
    }

    @PostMapping("/admin/deactivate-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateExpiredSubscriptions() {
        subscriptionService.deactivateExpiredSubscription();
        return ResponseEntity.ok(new ApiResponse(
                "Expired subscriptions deactivated successfully"
                ,true));
    }

}
