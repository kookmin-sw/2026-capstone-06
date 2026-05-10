package com.capstone.pethouse.domain.auth.service;

import com.capstone.pethouse.domain.User.entity.User;
import com.capstone.pethouse.domain.User.repository.UserRepository;
import com.capstone.pethouse.domain.auth.dto.LoginRequest;
import com.capstone.pethouse.domain.auth.dto.TokenResponse;
import com.capstone.pethouse.domain.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse loginWeb(LoginRequest request) {
        return processLogin(request);
    }

    @Transactional
    public TokenResponse loginApp(LoginRequest request) {
        return processLogin(request);
    }

    private TokenResponse processLogin(LoginRequest request) {
        User user = userRepository.findByMemberId(request.memberId())
                .orElseThrow(() -> new IllegalArgumentException("로그인 실패"));

        if (!passwordEncoder.matches(request.memberPw(), user.getMemberPw())) {
            throw new IllegalArgumentException("로그인 실패");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getMemberId(), user.getRoleCode().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getMemberId());

        user.updateRefreshToken(refreshToken);

        return TokenResponse.success(user.getRoleCode().name(), accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        String memberId = jwtTokenProvider.getMemberIdFromToken(refreshToken);
        User user = userRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new IllegalArgumentException("저장된 Refresh Token과 일치하지 않습니다.");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getMemberId(), user.getRoleCode().name());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getMemberId());

        user.updateRefreshToken(newRefreshToken);

        return TokenResponse.success(user.getRoleCode().name(), newAccessToken, newRefreshToken);
    }
}
