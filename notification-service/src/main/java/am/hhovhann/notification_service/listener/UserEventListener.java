package am.hhovhann.notification_service.listener;

import am.hhovhann.notification_service.events.UserLoggedEvent;
import am.hhovhann.notification_service.events.UserRegisteredEvent;
import am.hhovhann.notification_service.events.UserRetrievedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserEventListener {
  private final ObjectMapper objectMapper;

  public UserEventListener(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @KafkaListener(topics = "user.registered", groupId = "notification-group")
  public void handleUserRegistered(String userJson) {
    try {
      UserRegisteredEvent event = objectMapper.readValue(userJson, UserRegisteredEvent.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    System.out.println("Received user registered event: " + userJson);
    // parse JSON and send email or store notification
  }

  @KafkaListener(topics = "user.logged", groupId = "notification-group")
  public void handleUserLogged(String userJson) {
    try {
      UserLoggedEvent event = objectMapper.readValue(userJson, UserLoggedEvent.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    System.out.println("Received user registered event: " + userJson);
    // parse JSON and send email or store notification
  }

  @KafkaListener(topics = "user.retrieved", groupId = "notification-group")
  public void handleUserRetrieved(String userJson) {
    try {
      UserRetrievedEvent event = objectMapper.readValue(userJson, UserRetrievedEvent.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    System.out.println("Received user registered event: " + userJson);
    // parse JSON and send email or store notification
  }
}
