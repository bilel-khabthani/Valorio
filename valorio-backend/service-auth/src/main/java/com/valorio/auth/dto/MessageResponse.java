package com.valorio.auth.dto;

public record MessageResponse(
        String message,
        boolean success
) {
    public static MessageResponse of(String message) {
        return new MessageResponse(message, true);
    }

    public static MessageResponse error(String message) {
        return new MessageResponse(message, false);
    }
}
