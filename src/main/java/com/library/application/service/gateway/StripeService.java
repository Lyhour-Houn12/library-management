package com.library.application.service.gateway;

import com.library.application.domain.PaymentGateway;
import com.library.application.domain.PaymentStatus;
import com.library.application.domain.PaymentType;
import com.library.application.entity.Payment;
import com.library.application.entity.Subscription;
import com.library.application.entity.User;
import com.library.application.exception.PaymentException;
import com.library.application.payload.response.PaymentLinkResponse;
import com.library.application.repository.PaymentRepository;
import com.library.application.repository.SubscriptionRepository;
import com.stripe.StripeClient;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class StripeService {
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Value("${stripe.api.key:}")
    private String stripeSecretKey;

    @Value("${stripe.publishable.key:}")
    private String stripePublishableKey;

    @Value("${stripe.callback.base-url:http://localhost:5173}")
    private String callbackBaseUrl;



    // 1. Build Checkout Session
    // 2. Set success and cancel URLs
    // 3. Set the amount and currency
    // 4. Add metadata:
    //      userId
    //      paymentId
    // 5. Create the session
    // 6. Save the Stripe session ID in payment
    // 7. Return the checkout URL

    public PaymentLinkResponse createPaymentLink(User user, Payment payment) {

        validateConfiguration();
        try{
            StripeClient stripeClient  = new  StripeClient(stripeSecretKey);
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(callbackBaseUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(callbackBaseUrl + "/payment/cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(payment.getCurrency().toLowerCase())
                                                    .setUnitAmount((long) (payment.getAmount() * (new BigDecimal(100).intValue())))
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Library Subscription")
                                                                    .setDescription(payment.getDescription())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("paymentId", payment.getId().toString())
                    .putMetadata("userId", user.getId().toString())
                    .build();
            Session session = stripeClient.v1().checkout()
                    .sessions()
                    .create(params);
            payment.setGateway(PaymentGateway.STRIPE);
            payment.setGatewayOrderId(payment.getGatewayOrderId());
            payment.setStatus(PaymentStatus.PENDING);

            return new PaymentLinkResponse(
                    session.getUrl(),
                    session.getId()
            );
        }catch (Exception e){
            log.error("Failed to create Stripe payment link: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create payment link: " + e.getMessage());
        }
    }

    public boolean verifyPayment(String stripeSessionId){
        Session session = fetchPaymentDetails(stripeSessionId);
        return "paid".equals(session.getPaymentStatus());
    }

    public Session fetchPaymentDetails(String stripeSessionId){
        validateConfiguration();
        try{
            StripeClient stripeClient = new  StripeClient(stripeSecretKey);
            return stripeClient.v1()
                    .checkout()
                    .sessions()
                    .retrieve(stripeSessionId);
        } catch (Exception e) {
            log.error("Failed to fetch payment details: {}", e.getMessage(), e);
            throw new PaymentException("Failed to fetch payment details: " + e.getMessage());
        }
    }
    public boolean isValidPayment(Long paymentId){

        try{
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));
            Session session = fetchPaymentDetails(payment.getGatewayOrderId());
            log.info("Session ID: {}", session.getId());
            log.info("Payment Status: {}", session.getPaymentStatus());
            log.info("Amount Total: {}", session.getAmountTotal());

            String status   = session.getPaymentStatus();
            Long amountTotal = session.getAmountTotal();

            // 1. Check payment was actually completed
            if(!"paid".equalsIgnoreCase(status)){
                log.warn("Payment not completed. Current Status: {}", status);
                return false;
            }
            // 2. Check amount matches expected value based on payment type
            if(payment.getPaymentType() == PaymentType.MEMBERSHIP){
                Subscription subscription = payment.getSubscription();
                if(subscription == null){
                    log.warn("Membership payment missing linked subscription: {}", paymentId);
                    return false;
                }
                subscriptionRepository.findById(subscription.getId())
                        .orElseThrow(() -> new PaymentException("Subscription not found: " + subscription.getId()));

                Double expectedAmount = subscription.getPlan().getPrice();

                if (amountTotal == null) {
                    log.warn("Stripe amount is null");
                    return false;
                }

                double amountInDollars = amountTotal / 100.0;

                log.info("Expected Amount : {}", expectedAmount);
                log.info("Stripe Amount   : {}", amountInDollars);
                log.info("Difference      : {}", Math.abs(amountInDollars - expectedAmount));

                boolean matched = Math.abs(amountInDollars - expectedAmount) <= 0.1;
                log.info("Amount Matched  : {}", matched);

                return matched;
            }

            log.warn("Unknown payment type for validation: {}", payment.getPaymentType());
            return true;

        }catch (Exception e){
            log.error("Failed to validate payment: {}", e.getMessage(), e);
            throw new PaymentException("Failed to validate payment: " + e.getMessage());
        }
    }



    private boolean isConfigured(){
        log.info("stripeSecretKey = [{}]", stripeSecretKey);
        return stripeSecretKey != null && !stripeSecretKey.isEmpty()
                && callbackBaseUrl != null && !callbackBaseUrl.isEmpty()
                && (callbackBaseUrl.startsWith("http://") || callbackBaseUrl.startsWith("https://"));
    }
    private void validateConfiguration(){
        if(!isConfigured()){
            throw new PaymentException("Stripe is not configured. Please set    stripe secret key");
        }
    }
}
