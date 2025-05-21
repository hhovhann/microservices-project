package am.hhovhann.chat_service.controller;

import am.hhovhann.chat_service.dto.ChatMessageRequest;
import am.hhovhann.chat_service.dto.ChatMessageResponse;
import am.hhovhann.chat_service.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/chat")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatMessageResponse> handleMessage(@RequestBody ChatMessageRequest chatMessageRequest) {
        return ResponseEntity.ok(chatService.processUserMessage(chatMessageRequest));
    }
}
