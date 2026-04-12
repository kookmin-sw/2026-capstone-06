package com.capstone.pethouse.domain.auth.dto;

public record TokenResponse(
        String message,
        String role,
        String accessToken
) {
    public static TokenResponse success(String role, String accessToken) {
        return new TokenResponse("로그인 성공", role, accessToken);
    }

    public static TokenResponse success(String role) {
        return new TokenResponse("로그인 성공", role, null);
    }
}
