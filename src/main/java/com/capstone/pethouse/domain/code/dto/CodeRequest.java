package com.capstone.pethouse.domain.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CodeRequest(
        String code,
        @JsonProperty("group_code") String groupCode,
        @JsonProperty("code_name") String codeName
) {
}
