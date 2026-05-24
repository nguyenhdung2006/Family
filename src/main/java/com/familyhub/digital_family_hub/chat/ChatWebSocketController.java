package com.familyhub.digital_family_hub.chat;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;

    public ChatWebSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/rooms/{roomId}")
    public void sendMessage(
        @DestinationVariable UUID roomId,
        @Valid @Payload MessageDTO.SendRequest request,
        Principal principal
    ) {
        chatService.sendMessage(roomId, request, principal);
    }
}
