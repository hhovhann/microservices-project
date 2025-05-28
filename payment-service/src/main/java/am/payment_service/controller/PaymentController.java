package am.payment_service.controller;

import am.payment_service.dto.PaymentRequest;
import am.payment_service.enums.OrderStatus;
import am.payment_service.model.ServiceOrder;
import am.payment_service.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/orders")
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final StripeService stripeService;
    // In a real application, you would use a database (e.g., H2, PostgreSQL)
    // to store service orders. For this example, we use an in-memory map.
    private final Map<String, ServiceOrder> serviceOrders = new ConcurrentHashMap<>();

    public PaymentController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    /**
     * Endpoint to create a new service order and a corresponding Stripe PaymentIntent.
     *
     * @param order The ServiceOrder object containing details like description, amount, and currency.
     * @return A map containing the order ID and the client secret for the PaymentIntent.
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createServiceOrder(@RequestBody ServiceOrder order) {
        try {
            // Generate a unique ID for the service order
            String orderId = UUID.randomUUID().toString();
            order.setId(orderId);
            order.setStatus(OrderStatus.PENDING.name()); // Set initial status

            // Create a PaymentIntent with Stripe
            PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                    order.getAmount(),
                    order.getCurrency(),
                    order.getDescription()
            );

            order.setStripePaymentIntentId(paymentIntent.getId()); // Store Stripe PaymentIntent ID
            serviceOrders.put(orderId, order); // Save the order (in-memory for this example)

            Map<String, String> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("clientSecret", paymentIntent.getClientSecret()); // Send client secret to frontend
            response.put("publishableKey", stripeService.retrieveStripeSecretKey()); // For convenience, though usually frontend has this

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            // Log the exception for debugging
            log.error("StripeException during PaymentIntent creation: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create payment intent: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error during order creation: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Endpoint to confirm a payment for an existing service order.
     * This endpoint receives the order ID and the Stripe Payment Method ID from the client.
     *
     * @param paymentRequest DTO containing orderId and paymentMethodId.
     * @return A map indicating the payment status.
     */
    @PostMapping("/confirm-payment")
    public ResponseEntity<Map<String, String>> confirmPayment(@RequestBody PaymentRequest paymentRequest) {
        String orderId = paymentRequest.orderId();
        String paymentMethodId = paymentRequest.paymentMethodId();

        ServiceOrder order = serviceOrders.get(orderId);
        if (order == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Service order not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        try {
            // Confirm the PaymentIntent with the provided payment method
            PaymentIntent paymentIntent = stripeService.confirmPaymentIntent(
                    order.getStripePaymentIntentId(),
                    paymentMethodId
            );

            // Update the order status based on the PaymentIntent status
            Map<String, String> response = new HashMap<>();
            String paymentIntentStatus = paymentIntent.getStatus();
            response.put("paymentIntentStatus", paymentIntentStatus);

            switch (paymentIntentStatus) {
                case "succeeded":
                    order.setStatus(OrderStatus.PAID.name());
                    response.put("message", "Payment succeeded!");
                    break;
                case "requires_action":
                    // This means additional action (e.g., 3D Secure authentication) is required
                    response.put("message", "Payment requires additional action.");
                    response.put("clientSecret", paymentIntent.getClientSecret()); // Send client secret for client-side handling
                    break;
                case "requires_payment_method":
                    response.put("message", "Payment failed, requires a new payment method.");
                    order.setStatus(OrderStatus.FAILED.name());
                    break;
                case "canceled":
                    response.put("message", "Payment was canceled.");
                    order.setStatus(OrderStatus.CANCELLED.name());
                    break;
                default:
                    response.put("message", "Payment status: " + paymentIntentStatus);
                    order.setStatus(OrderStatus.PENDING.name()); // Or a more specific status
                    break;
            }
            serviceOrders.put(orderId, order); // Update order status in map

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            log.error("StripeException during PaymentIntent confirmation: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to confirm payment: " + e.getMessage());
            // Optionally update order status to FAILED here
            order.setStatus(OrderStatus.FAILED.name());
            serviceOrders.put(orderId, order);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error during payment confirmation: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Endpoint to get the status of a service order.
     *
     * @param orderId The ID of the service order.
     * @return The ServiceOrder object.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ServiceOrder> getServiceOrder(@PathVariable String orderId) {
        ServiceOrder order = serviceOrders.get(orderId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(order);
    }
}
