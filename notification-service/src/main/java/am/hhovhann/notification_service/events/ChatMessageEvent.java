package am.hhovhann.notification_service.events;

public record ChatMessageEvent(String userId, String message, String timestamp)  {}
