package com.capstone.pethouse.domain.User.service;

import com.capstone.pethouse.domain.User.dto.*;
import com.capstone.pethouse.domain.User.entity.User;
import com.capstone.pethouse.domain.User.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User createTestUser() {
        User user = User.ofUser("user01", "encoded", "홍길동", "010-1111-1111");
        ReflectionTestUtils.setField(user, "seq", 1L);
        ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now());
        return user;
    }

    @Test
    @DisplayName("회원가입 성공")
    void registerSuccess() {
        MemberRequest request = new MemberRequest(null, "user01", "1234", "홍길동", "010-1111-1111", null);
        User saved = createTestUser();

        given(userRepository.existsByMemberId("user01")).willReturn(false);
        given(passwordEncoder.encode("1234")).willReturn("encoded");
        given(userRepository.save(any(User.class))).willReturn(saved);

        MemberResponse response = memberService.register(request);

        assertThat(response.memberId()).isEqualTo("user01");
        assertThat(response.roleCode()).isEqualTo("USER");
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 아이디")
    void registerFailDuplicate() {
        MemberRequest request = new MemberRequest(null, "user01", "1234", "홍길동", "010-1111-1111", null);

        given(userRepository.existsByMemberId("user01")).willReturn(true);

        assertThatThrownBy(() -> memberService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 아이디입니다.");
    }

    @Test
    @DisplayName("아이디 중복확인 - 사용 가능")
    void checkIdAvailable() {
        given(userRepository.existsByMemberId("newuser")).willReturn(false);

        assertThat(memberService.checkIdAvailable("newuser")).isTrue();
    }

    @Test
    @DisplayName("아이디 찾기 성공")
    void findIdSuccess() {
        User user = createTestUser();
        FindIdRequest request = new FindIdRequest("홍길동", "010-1111-1111");

        given(userRepository.findByMemberNameAndMemberPhone("홍길동", "010-1111-1111"))
                .willReturn(Optional.of(user));

        assertThat(memberService.findId(request)).isEqualTo("user01");
    }

    @Test
    @DisplayName("아이디 찾기 실패")
    void findIdFail() {
        FindIdRequest request = new FindIdRequest("없는사람", "010-9999-9999");

        given(userRepository.findByMemberNameAndMemberPhone("없는사람", "010-9999-9999"))
                .willReturn(Optional.empty());

        assertThat(memberService.findId(request)).isNull();
    }

    @Test
    @DisplayName("회원 정보 검증 성공")
    void verifyUserSuccess() {
        User user = createTestUser();
        VerifyUserRequest request = new VerifyUserRequest("user01", "홍길동", "010-1111-1111");

        given(userRepository.findByMemberIdAndMemberNameAndMemberPhone("user01", "홍길동", "010-1111-1111"))
                .willReturn(Optional.of(user));

        assertThat(memberService.verifyUser(request)).isTrue();
    }

    @Test
    @DisplayName("비밀번호 초기화 성공")
    void resetPasswordSuccess() {
        User user = createTestUser();
        ResetPasswordRequest request = new ResetPasswordRequest("user01", "홍길동", "010-1111-1111", "5678");

        given(userRepository.findByMemberIdAndMemberNameAndMemberPhone("user01", "홍길동", "010-1111-1111"))
                .willReturn(Optional.of(user));
        given(passwordEncoder.encode("5678")).willReturn("newEncoded");

        boolean result = memberService.resetPassword(request);

        assertThat(result).isTrue();
        assertThat(user.getMemberPw()).isEqualTo("newEncoded");
    }

    @Test
    @DisplayName("회원 삭제")
    void deleteMember() {
        User user = createTestUser();
        given(userRepository.findByMemberId("user01")).willReturn(Optional.of(user));

        memberService.deleteMember(null, "user01");

        verify(userRepository).delete(user);
    }
}
