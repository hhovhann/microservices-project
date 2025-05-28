package am.payment_service.enums;


// Enum for order status for better type safety (optional, but good practice)
public enum OrderStatus {
    PENDING,
    PAID,
    CANCELLED,
    FAILED
}
