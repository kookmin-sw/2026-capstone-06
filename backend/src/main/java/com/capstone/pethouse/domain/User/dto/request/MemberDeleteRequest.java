package com.capstone.pethouse.domain.User.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MemberDeleteRequest(
                Long seq,
                @JsonProperty("member_id") String memberId) {
}
