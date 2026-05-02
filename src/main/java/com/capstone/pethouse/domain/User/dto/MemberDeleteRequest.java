package com.capstone.pethouse.domain.User.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MemberDeleteRequest(
        Long seq,
        @JsonProperty("member_id") String memberId
) {
}
