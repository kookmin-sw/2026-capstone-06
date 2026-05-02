package com.capstone.pethouse.domain.User.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyUserRequest(
        @NotBlank(message = "아이디는 필수 입력 값입니다.") 
        String memberId,

        @NotBlank(message = "이름은 필수 입력 값입니다.") 
        String memberName,

        @NotBlank(message = "전화번호는 필수 입력 값입니다.") 
        String memberPhone
) {
}
