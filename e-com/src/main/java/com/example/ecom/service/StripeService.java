package com.example.ecom.service;

import com.example.ecom.payload.StripePaymentDto;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

public interface StripeService {

    Session createCheckoutSession(StripePaymentDto stripePaymentDto) throws StripeException;
}
