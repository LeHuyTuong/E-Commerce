package com.example.ecom.service.impl;

import com.example.ecom.payload.StripePaymentDto;
import com.example.ecom.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class StripeServiceImpl implements StripeService {

    @Value("${stripe.secret.key:sk_test_placeholder}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Value("${frontend.url:http://localhost:5173/}")
    private String frontendUrl;

    @Override
    public Session createCheckoutSession(StripePaymentDto stripePaymentDto) throws StripeException {
        // Create parameters for Stripe Checkout Session (Redirect Flow)
        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendUrl + "payment/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "payment/stripe")
                .setCustomerEmail(stripePaymentDto.getEmail())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(stripePaymentDto.getCurrency())
                                                .setUnitAmount(stripePaymentDto.getAmount()) // Amount in cents
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Thanh toán đơn hàng EcomStore")
                                                                .setDescription(stripePaymentDto.getDescription())
                                                                .build())
                                                .build())
                                .build())
                // Store metadata for reconciliation after redirect
                .putMetadata("addressId", String.valueOf(stripePaymentDto.getAddress().getAddressId()))
                .build();

        return Session.create(params);
    }
}
