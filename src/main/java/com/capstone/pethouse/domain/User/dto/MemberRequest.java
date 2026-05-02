package com.capstone.pethouse.domain.User.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record MemberRequest(
        Long seq,

        @NotBlank(message = "아이디는 필수 입력 값입니다.")
        @JsonProperty("member_id") String memberId,

        @JsonProperty("member_pw") String memberPw,

        @NotBlank(message = "이름은 필수 입력 값입니다.")
        @JsonProperty("member_name") String memberName,

        @NotBlank(message = "전화번호는 필수 입력 값입니다.")
        @JsonProperty("member_phone") String memberPhone,

        @JsonProperty("role_code") String roleCode
) {
}
