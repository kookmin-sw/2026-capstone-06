package com.capstone.pethouse.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String memberId,
        @NotBlank String memberPw
) {
}
