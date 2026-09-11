package com.lagermanagement.space.web.dto;

import java.time.LocalDateTime;

public record ApiErrorDto(int status, String message, LocalDateTime timestamp, Object details) {

    public static ApiErrorDto of(int status, String message) {
        return new ApiErrorDto(status, message, LocalDateTime.now(), null);
    }

    public static ApiErrorDto of(int status, String message, Object details) {
        return new ApiErrorDto(status, message, LocalDateTime.now(), details);
    }
}
