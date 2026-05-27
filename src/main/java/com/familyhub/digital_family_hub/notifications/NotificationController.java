package com.familyhub.digital_family_hub.notifications;

import com.familyhub.digital_family_hub.shared.api.ApiResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<List<NotificationDTO.Response>> listNotifications(Principal principal) {
        return ApiResponse.ok(notificationService.listNotifications(principal));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NotificationDTO.Response> createNotification(
        @Valid @RequestBody NotificationDTO.Request request,
        Principal principal
    ) {
        return ApiResponse.created(notificationService.createNotification(request, principal));
    }

    @PutMapping("/{id}")
    public ApiResponse<NotificationDTO.Response> updateNotification(
        @PathVariable UUID id,
        @Valid @RequestBody NotificationDTO.Request request,
        Principal principal
    ) {
        return ApiResponse.ok(notificationService.updateNotification(id, request, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id, Principal principal) {
        notificationService.deleteNotification(id, principal);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationDTO.Response> markRead(@PathVariable UUID id, Principal principal) {
        return ApiResponse.ok(notificationService.markRead(id, principal));
    }
}
