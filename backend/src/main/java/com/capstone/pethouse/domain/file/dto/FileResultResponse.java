package com.capstone.pethouse.domain.file.dto;

public record FileResultResponse(boolean result, String message) {
    public static FileResultResponse ok(String message) {
        return new FileResultResponse(true, message);
    }

    public static FileResultResponse fail(String message) {
        return new FileResultResponse(false, message);
    }
}
