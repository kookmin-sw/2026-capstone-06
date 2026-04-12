package com.capstone.pethouse.domain.auth.service;

import com.capstone.pethouse.domain.User.entity.User;
import com.capstone.pethouse.domain.User.repository.UserRepository;
import com.capstone.pethouse.domain.auth.dto.LoginRequest;
import com.capstone.pethouse.domain.auth.dto.TokenResponse;
import com.capstone.pethouse.domain.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenResponse loginWeb(LoginRequest request) {
        User user = userRepository.findByMemberId(request.memberId())
                .orElseThrow(() -> new IllegalArgumentException("로그인 실패"));

        if (!passwordEncoder.matches(request.memberPw(), user.getMemberPw())) {
            throw new IllegalArgumentException("로그인 실패");
        }

        return TokenResponse.success(user.getRoleCode().name());
    }

    public TokenResponse loginApp(LoginRequest request) {
        User user = userRepository.findByMemberId(request.memberId())
                .orElseThrow(() -> new IllegalArgumentException("로그인 실패"));

        if (!passwordEncoder.matches(request.memberPw(), user.getMemberPw())) {
            throw new IllegalArgumentException("로그인 실패");
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getMemberId(), user.getRoleCode().name()
        );

        return TokenResponse.success(user.getRoleCode().name(), accessToken);
    }
}
