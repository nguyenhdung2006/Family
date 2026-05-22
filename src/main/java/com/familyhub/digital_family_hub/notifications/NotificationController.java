package com.familyhub.digital_family_hub.notifications;

import com.familyhub.digital_family_hub.shared.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ApiResponse<List<NotificationDTO.Response>> listNotifications() {
        return ApiResponse.ok(notificationService.listNotifications());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NotificationDTO.Response> createNotification(
        @Valid @RequestBody NotificationDTO.Request request
    ) {
        return ApiResponse.created(notificationService.createNotification(request));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationDTO.Response> markRead(@PathVariable UUID id) {
        return ApiResponse.ok(notificationService.markRead(id));
    }
}
