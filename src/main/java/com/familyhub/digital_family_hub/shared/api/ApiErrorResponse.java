package com.familyhub.digital_family_hub.shared.api;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
    boolean success,
    String errorCode,
    String message,
    List<FieldErrorDetail> errors,
    Instant timestamp,
    String path
) {
    public static ApiErrorResponse of(String errorCode, String message, String path) {
        return new ApiErrorResponse(false, errorCode, message, List.of(), Instant.now(), path);
    }

    public static ApiErrorResponse validation(String message, List<FieldErrorDetail> errors, String path) {
        return new ApiErrorResponse(false, "VALIDATION_ERROR", message, errors, Instant.now(), path);
    }

    public record FieldErrorDetail(String field, String message) {
    }
}
