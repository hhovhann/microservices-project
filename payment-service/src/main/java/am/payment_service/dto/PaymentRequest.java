package am.payment_service.dto;

public record PaymentRequest(String orderId, String paymentMethodId) {
}
