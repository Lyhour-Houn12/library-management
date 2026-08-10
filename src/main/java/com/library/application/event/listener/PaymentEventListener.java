package com.library.application.event.listener;

import com.library.application.event.PaymentSuccessEvent;
import com.library.application.service.FineService;
import com.library.application.service.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {
    private final SubscriptionService subscriptionService;
    private final FineService fineService;

    @Async
    @EventListener
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Received Payment Success Event: paymentId={}, type:{}, amount:{}", event.getPaymentId(), event.getType(), event.getAmount());


        switch (event.getType()){
            case FINE:
            case DAMAGED_BOOK_PENALTY:
            case LOST_BOOK_PENALTY:
                fineService.markAsPaid(event.getFineId(), event.getTransactionId());
                break;
            case MEMBERSHIP:
                subscriptionService.activateSubscription(event.getSubscriptionId(), event.getPaymentId());
                break;
                default:
                    log.warn("Unhandled PaymentEvent type: {}", event.getType());
        }
    }
}
