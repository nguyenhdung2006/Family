package com.familyhub.digital_family_hub.chat;

import com.familyhub.digital_family_hub.shared.api.ApiResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/rooms")
    public ApiResponse<List<MessageDTO.RoomResponse>> listRooms(Principal principal) {
        return ApiResponse.ok(chatService.listRooms(principal));
    }

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MessageDTO.RoomResponse> createRoom(
        @Valid @RequestBody MessageDTO.RoomRequest request,
        Principal principal
    ) {
        return ApiResponse.created(chatService.createRoom(request, principal));
    }

    @PutMapping("/rooms/{roomId}")
    public ApiResponse<MessageDTO.RoomResponse> updateRoom(
        @PathVariable UUID roomId,
        @Valid @RequestBody MessageDTO.RoomRequest request,
        Principal principal
    ) {
        return ApiResponse.ok(chatService.updateRoom(roomId, request, principal));
    }

    @GetMapping("/rooms/{roomId}")
    public ApiResponse<List<MessageDTO.Response>> listMessages(
        @PathVariable UUID roomId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size,
        Principal principal
    ) {
        return ApiResponse.ok(chatService.listMessages(roomId, page, size, principal));
    }

    @PostMapping("/rooms/{roomId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MessageDTO.Response> sendMessage(
        @PathVariable UUID roomId,
        @Valid @RequestBody MessageDTO.SendRequest request,
        Principal principal
    ) {
        return ApiResponse.created(chatService.sendMessage(roomId, request, principal));
    }

    @PatchMapping("/{messageId}/seen")
    public ApiResponse<MessageDTO.Response> markSeen(@PathVariable UUID messageId, Principal principal) {
        return ApiResponse.ok(chatService.markSeen(messageId, principal));
    }
}
