package com.capstone.pethouse.domain.User.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyUserRequest(
        @NotBlank String memberId,
        @NotBlank String memberName,
        @NotBlank String memberPhone
) {
}
