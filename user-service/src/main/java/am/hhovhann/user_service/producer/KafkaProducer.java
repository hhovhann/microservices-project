package am.hhovhann.user_service.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
	private final KafkaTemplate<String, String> kafkaTemplate;

	public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void sendUserRegisteredEvent(String userId, String userJson) {
		kafkaTemplate.send("user.registered", userId, userJson);
	}

	public void sendUserLoggedEvent(String userId, String loggedUserJson) {
		kafkaTemplate.send("user.logged", userId, loggedUserJson);
	}

	public void sendUserRetrievedEvent(String userId, String userJson) {
		kafkaTemplate.send("user.retrived", userId, userJson);
	}
}
