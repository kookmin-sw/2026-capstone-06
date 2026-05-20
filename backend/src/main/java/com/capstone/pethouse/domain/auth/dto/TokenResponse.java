package com.capstone.pethouse.domain.auth.dto;

public record TokenResponse(
        String message,
        String role,
        String accessToken,
        String refreshToken
) {
    public static TokenResponse success(String role, String accessToken, String refreshToken) {
        return new TokenResponse("로그인 성공", role, accessToken, refreshToken);
    }
}
