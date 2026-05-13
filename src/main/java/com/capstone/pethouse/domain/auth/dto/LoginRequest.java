package com.capstone.pethouse.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
                @NotBlank(message = "아이디는 필수 입력 값입니다.") String memberId,
                @NotBlank(message = "비밀번호는 필수 입력 값입니다.") String memberPw) {
}
