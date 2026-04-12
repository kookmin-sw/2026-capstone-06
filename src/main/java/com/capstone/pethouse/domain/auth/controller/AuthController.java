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

@RequiredArgsConstructor
@RequestMapping("/member")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> loginWeb(@Valid @RequestBody LoginRequest request) {
        try {
            TokenResponse response = authService.loginWeb(request);
            return ResponseEntity.ok(Map.of("message", response.message(), "role", response.role()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인 실패"));
        }
    }

    @PostMapping("/login-app")
    public ResponseEntity<?> loginApp(@Valid @RequestBody LoginRequest request) {
        try {
            TokenResponse response = authService.loginApp(request);
            return ResponseEntity.ok(Map.of(
                    "message", response.message(),
                    "role", response.role(),
                    "accessToken", response.accessToken()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인 실패"));
        }
    }
}
