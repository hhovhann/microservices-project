package am.hhovhann.notification_service.listener;

import am.hhovhann.notification_service.events.ChatMessageEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChatEventListener {
  private final ObjectMapper objectMapper;

  public ChatEventListener(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @KafkaListener(topics = "chat.messaged", groupId = "notification-group")
  public void handleChatMessaged(String messageJson) {
    try {
      ChatMessageEvent event = objectMapper.readValue(messageJson, ChatMessageEvent.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    System.out.println("Received chat messaged event: " + messageJson);
    // parse JSON and send email or store notification
  }
}
