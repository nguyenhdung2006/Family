package com.familyhub.digital_family_hub.shared.api;

import java.time.Instant;

public record ApiResponse<T>(
    boolean success,
    T data,
    String message,
    Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, "OK", Instant.now());
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, data, "Created", Instant.now());
    }

    public static <T> ApiResponse<T> message(T data, String message) {
        return new ApiResponse<>(true, data, message, Instant.now());
    }
}
