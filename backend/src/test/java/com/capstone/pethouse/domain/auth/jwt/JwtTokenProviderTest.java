package com.capstone.pethouse.domain.auth.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        String testSecret = Base64.getEncoder().encodeToString(
                "test-secret-key-that-is-at-least-32-bytes-long!!".getBytes()
        );
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiry", 1800000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiry", 1209600000L);
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("Access Token 생성 및 검증")
    void createAndValidateAccessToken() {
        String token = jwtTokenProvider.createAccessToken("user01", "USER");

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getMemberIdFromToken(token)).isEqualTo("user01");
    }

    @Test
    @DisplayName("Refresh Token 생성 및 검증")
    void createAndValidateRefreshToken() {
        String token = jwtTokenProvider.createRefreshToken("user01");

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getMemberIdFromToken(token)).isEqualTo("user01");
    }

    @Test
    @DisplayName("Access Token으로 Authentication 객체 생성")
    void getAuthentication() {
        String token = jwtTokenProvider.createAccessToken("user01", "USER");

        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo("user01");
        assertThat(authentication.getAuthorities()).hasSize(1);
        assertThat(authentication.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("잘못된 토큰 검증 실패")
    void validateInvalidToken() {
        assertThat(jwtTokenProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void validateExpiredToken() {
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiry", -1000L);
        jwtTokenProvider.init();
        String token = jwtTokenProvider.createAccessToken("user01", "USER");

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }
}
