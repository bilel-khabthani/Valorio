package com.valorio.auth.dto;

public record MessageResponse(
        String message,
        boolean success,
        String devLink
) {
    public static MessageResponse of(String message) {
        return new MessageResponse(message, true, null);
    }

    public static MessageResponse of(String message, String devLink) {
        return new MessageResponse(message, true, devLink);
    }

    public static MessageResponse error(String message) {
        return new MessageResponse(message, false, null);
    }
}

