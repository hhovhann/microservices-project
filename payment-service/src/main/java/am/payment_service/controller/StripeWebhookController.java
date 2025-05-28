package am.payment_service.controller;
import am.payment_service.enums.OrderStatus;
import am.payment_service.model.ServiceOrder;
import am.payment_service.service.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Webhook Controller for receiving and processing Stripe events.
 * IMPORTANT: In a production environment, we MUST verify the webhook signature
 * to ensure the event is truly from Stripe and has not been tampered with.
 */
@RestController
public class StripeWebhookController {
    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final StripeService stripeService;
    // This map should be the same instance as in ServiceOrderController
    // In a real app, this would be a shared database or service.
    private final Map<String, ServiceOrder> serviceOrders;

    public StripeWebhookController(StripeService stripeService) {
        this.stripeService = stripeService;
        // This is a simple way to share the map for this example.
        // In a real application, you would inject a service that manages orders
        // and persists them to a database.
        this.serviceOrders = new ConcurrentHashMap<>(); // Initialize or inject the shared map
    }

    // A simple way to get the shared map from ServiceOrderController for this example.
    // In a real application, you'd have a proper OrderRepository/Service managing this.
    // This method is for demonstration purposes only.
    public void setServiceOrdersMap(Map<String, ServiceOrder> serviceOrdersMap) {
        this.serviceOrders.putAll(serviceOrdersMap);
    }


    /**
     * Endpoint for Stripe webhooks. Stripe sends events here to notify your application
     * about changes in payment status, refunds, etc.
     *
     * @param payload The raw JSON payload of the webhook event.
     * @param sigHeader The Stripe-Signature header, used for verifying the event.
     * @return A ResponseEntity indicating success or failure.
     */
    @PostMapping("/stripe-webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;

        try {
            // IMPORTANT: Verify the event signature to ensure it's from Stripe
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            // Invalid signature
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid webhook signature");
        } catch (Exception e) {
            log.error("Error parsing webhook event: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error parsing webhook event");
        }

        // Handle the event
        switch (event.getType()) {
            case "payment_intent.succeeded":
                PaymentIntent paymentIntentSucceeded = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (paymentIntentSucceeded != null) {
                    log.info("PaymentIntent succeeded: {}", paymentIntentSucceeded.getId());
                    // Find the corresponding service order and update its status to PAID
                    serviceOrders.values().stream()
                            .filter(order -> paymentIntentSucceeded.getId().equals(order.getStripePaymentIntentId()))
                            .findFirst()
                            .ifPresent(order -> {
                                order.setStatus(OrderStatus.PAID.name());
                                log.info("Order {} status updated to PAID.", order.getId());
                            });
                }
                break;
            case "payment_intent.payment_failed":
                PaymentIntent paymentIntentFailed = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (paymentIntentFailed != null) {
                    log.info("PaymentIntent failed: {}", paymentIntentFailed.getId());
                    // Find the corresponding service order and update its status to FAILED
                    serviceOrders.values().stream()
                            .filter(order -> paymentIntentFailed.getId().equals(order.getStripePaymentIntentId()))
                            .findFirst()
                            .ifPresent(order -> {
                                order.setStatus(OrderStatus.FAILED.name());
                                log.info("Order {} status updated to FAILED.", order.getId());
                            });
                }
                break;
            case "charge.refunded":
                // Handle refunds if necessary
                log.info("Charge refunded event received.");
                break;
            // ... handle other event types as needed
            default:
                log.info("Unhandled event type: {}", event.getType());
                break;
        }

        return ResponseEntity.ok("Webhook received");
    }
}
