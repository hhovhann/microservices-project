package am.hhovhann.chat_service.service;

import am.hhovhann.chat_service.dto.ChatMessageRequest;
import am.hhovhann.chat_service.dto.ChatMessageResponse;

public interface ChatService {
    ChatMessageResponse processUserMessage(ChatMessageRequest chatMessageRequest);
}
