package am.payment_service.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Represents a service order in our application.
 * This model holds details about the order and its payment status.
 */
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor // Lombok annotation to generate a constructor with all fields
public class ServiceOrder {
    private String id;
    private String description;
    private long amount; // Amount in cents (e.g., $10.00 is 1000)
    private String currency; // e.g., "usd"
    private String status; // e.g., "PENDING", "PAID", "CANCELLED"
    private String stripePaymentIntentId; // ID of the associated Stripe PaymentIntent

}
