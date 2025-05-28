package am.payment_service.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class to interact with the Stripe API.
 * Handles creation, confirmation, and retrieval of PaymentIntents.
 */
@Service
public class StripeServiceImpl implements StripeService {
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    /**
     * Initializes the Stripe API key when the service is constructed.
     */
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Creates a Stripe PaymentIntent.
     * A PaymentIntent is an object that represents your intent to collect payment from a customer.
     *
     * @param amount      The amount to collect, in the smallest currency unit (e.g., cents for USD).
     * @param currency    The three-letter ISO currency code (e.g., "usd").
     * @param description A description of the payment.
     * @return The created PaymentIntent object.
     * @throws StripeException If there's an error interacting with the Stripe API.
     */
    @Override
    public PaymentIntent createPaymentIntent(long amount, String currency, String description) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setDescription(description)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();
        return PaymentIntent.create(params);
    }

    /**
     * Confirms an existing Stripe PaymentIntent.
     * This step is typically done after the client has provided a payment method.
     *
     * @param paymentIntentId The ID of the PaymentIntent to confirm.
     * @param paymentMethodId The ID of the payment method (e.g., card, bank account) provided by the client.
     * @return The confirmed PaymentIntent object.
     * @throws StripeException If there's an error interacting with the Stripe API.
     */
    @Override
    public PaymentIntent confirmPaymentIntent(String paymentIntentId, String paymentMethodId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder()
                .setPaymentMethod(paymentMethodId)
                .build();

        return paymentIntent.confirm(params);
    }

    /**
     * Retrieves a Stripe PaymentIntent by its ID.
     * Useful for checking the status of a payment.
     *
     * @param paymentIntentId The ID of the PaymentIntent to retrieve.
     * @return The retrieved PaymentIntent object.
     * @throws StripeException If the PaymentIntent is not found or there's an API error.
     */
    @Override
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }

    @Override
    public String retrieveStripeSecretKey() {
        return stripeSecretKey.replace("sk_test_", "pk_test_");
    }
}
