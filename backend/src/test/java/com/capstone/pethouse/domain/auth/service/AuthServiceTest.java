package com.capstone.pethouse.domain.auth.service;

import com.capstone.pethouse.domain.User.entity.User;
import com.capstone.pethouse.domain.User.repository.UserRepository;
import com.capstone.pethouse.domain.auth.dto.LoginRequest;
import com.capstone.pethouse.domain.auth.dto.TokenResponse;
import com.capstone.pethouse.domain.auth.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("웹 로그인 성공")
    void loginWebSuccess() {
        LoginRequest request = new LoginRequest("user01", "1234");
        User user = User.ofUser("user01", "encoded", "홍길동", "010-1111-1111");

        given(userRepository.findByMemberId("user01")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("1234", "encoded")).willReturn(true);

        TokenResponse response = authService.loginWeb(request);

        assertThat(response.message()).isEqualTo("로그인 성공");
        assertThat(response.role()).isEqualTo("USER");
    }

    @Test
    @DisplayName("웹 로그인 실패 - 잘못된 비밀번호")
    void loginWebFailWrongPassword() {
        LoginRequest request = new LoginRequest("user01", "wrong");
        User user = User.ofUser("user01", "encoded", "홍길동", "010-1111-1111");

        given(userRepository.findByMemberId("user01")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);

        assertThatThrownBy(() -> authService.loginWeb(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("로그인 실패");
    }

    @Test
    @DisplayName("웹 로그인 실패 - 존재하지 않는 회원")
    void loginWebFailUserNotFound() {
        LoginRequest request = new LoginRequest("nobody", "1234");

        given(userRepository.findByMemberId("nobody")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.loginWeb(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("로그인 실패");
    }

    @Test
    @DisplayName("앱 로그인 성공 - JWT 토큰 반환")
    void loginAppSuccess() {
        LoginRequest request = new LoginRequest("user01", "1234");
        User user = User.ofUser("user01", "encoded", "홍길동", "010-1111-1111");

        given(userRepository.findByMemberId("user01")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("1234", "encoded")).willReturn(true);
        given(jwtTokenProvider.createAccessToken("user01", "USER")).willReturn("jwt-token");

        TokenResponse response = authService.loginApp(request);

        assertThat(response.message()).isEqualTo("로그인 성공");
        assertThat(response.role()).isEqualTo("USER");
        assertThat(response.accessToken()).isEqualTo("jwt-token");
    }
}
