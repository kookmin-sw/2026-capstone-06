package com.capstone.pethouse.domain.User.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MemberRequest(
        Long seq,
        @JsonProperty("member_id") String memberId,
        @JsonProperty("member_pw") String memberPw,
        @JsonProperty("member_name") String memberName,
        @JsonProperty("member_phone") String memberPhone,
        @JsonProperty("role_code") String roleCode
) {
}
