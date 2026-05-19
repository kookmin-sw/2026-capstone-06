package com.capstone.pethouse.domain.auth.controller;

import com.capstone.pethouse.domain.auth.dto.LoginRequest;
import com.capstone.pethouse.domain.auth.dto.TokenResponse;
import com.capstone.pethouse.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import com.capstone.pethouse.domain.auth.dto.TokenRefreshRequest;

@RequiredArgsConstructor
@RequestMapping("/member")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginWeb(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.loginWeb(request);
        return ResponseEntity.ok(Map.of(
                "message", response.message(),
                "role", response.role(),
                "accessToken", response.accessToken(),
                "refreshToken", response.refreshToken()
        ));
    }

    @PostMapping("/login-app")
    public ResponseEntity<Map<String, String>> loginApp(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.loginApp(request);
        return ResponseEntity.ok(Map.of(
                "message", response.message(),
                "role", response.role(),
                "accessToken", response.accessToken(),
                "refreshToken", response.refreshToken()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        TokenResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(Map.of(
                "message", "토큰 갱신 성공",
                "role", response.role(),
                "accessToken", response.accessToken(),
                "refreshToken", response.refreshToken()
        ));
    }
}
