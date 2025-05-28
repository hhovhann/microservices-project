package am.payment_service.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

/**
 * Service class to interact with the Stripe API.
 * Handles creation, confirmation, and retrieval of PaymentIntents.
 */
public interface StripeService {

    PaymentIntent createPaymentIntent(long amount, String currency, String description) throws StripeException;

    PaymentIntent confirmPaymentIntent(String paymentIntentId, String paymentMethodId) throws StripeException;

    PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException;

    String retrieveStripeSecretKey();
}
