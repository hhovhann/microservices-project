package am.hhovhann.chat_service.service;

import am.hhovhann.chat_service.dto.ChatMessageRequest;
import am.hhovhann.chat_service.dto.ChatMessageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for processing user messages using Spring AI and publishing events to Kafka.
 */
@Service
public class ChatServiceImpl implements ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // In-memory storage for chat history.
    // In a production environment, consider using a persistent store like a database or Redis.
    private final Map<String, List<Message>> chatHistories = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public ChatServiceImpl(ChatClient chatClient, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Processes a user message, interacts with the AI model while maintaining chat history, and
     * publishes the user message as an event to Kafka.
     *
     * @param chatMessageRequest The instance of chat message: ID of the user sending the message, and the
     *                           content of the user's message.
     * @return The AI's response to the user message.
     */
    @Override
    public ChatMessageResponse processUserMessage(ChatMessageRequest chatMessageRequest) {
        // 1. Get or initialize chat history for the user
        List<Message> userChatHistory =
                chatHistories.computeIfAbsent(chatMessageRequest.userId(), k -> new ArrayList<>());

        // 2. Add the current user message to the history
        UserMessage userMessage = new UserMessage(chatMessageRequest.message());
        userChatHistory.add(userMessage);

        // 3. Create a Prompt with the full chat history
        Prompt prompt = new Prompt(userChatHistory);

        ChatMessageResponse aiResponseContent = new ChatMessageResponse("");
        try {
            // 4. Call the AI model with the prompt containing the full chat history
            aiResponseContent = new ChatMessageResponse(chatClient.prompt(prompt).call().content());

            // 5. Add the AI's successful response to the history
            AssistantMessage assistantMessage = new AssistantMessage(aiResponseContent.content());
            userChatHistory.add(assistantMessage);

        } catch (NonTransientAiException e) {
            // Handle specific Spring AI exceptions, like quota errors (HTTP 429)
            log.error("Spring AI call failed for user {}: {}", chatMessageRequest.userId(), e.getMessage(), e);
            aiResponseContent = new ChatMessageResponse("An unexpected error occurred while processing your request. Please try again.");

            // Optionally, you might not add this error message to the chat history
            // if you don't want it to influence future AI responses.
            // For now, we'll add it to show the user the system's response.
            userChatHistory.add(new AssistantMessage(aiResponseContent.content()));
        } catch (Exception e) {
            // Catch any other unexpected exceptions during the AI call
            log.error(
                    "An unexpected error occurred during AI call for user {}: {}",
                    chatMessageRequest.userId(),
                    e.getMessage(),
                    e);
            aiResponseContent = new ChatMessageResponse("I'm sorry, but I'm currently unable to process your request due to an issue with my AI service. Please try again later or contact support if the problem persists.");
            userChatHistory.add(new AssistantMessage(aiResponseContent.content()));
        }

        // 6. Publish the user message event to Kafka
        try {
            kafkaTemplate.send("chat.messaged", objectMapper.writeValueAsString(chatMessageRequest));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // 7. Return the AI's response (or error message)
        return aiResponseContent;
    }

    /**
     * Clears the chat history for a specific user. This can be useful for starting a new conversation
     * session.
     *
     * @param userId The ID of the user whose chat history should be cleared.
     */
    public void clearChatHistory(String userId) {
        chatHistories.remove(userId);
    }
}

// TODO: Self Learning
// add frontend for registration, logged in, retrieval
// add notification service send email
// add swagger
// add docker, kuber support
// add grafana, prometheus support
