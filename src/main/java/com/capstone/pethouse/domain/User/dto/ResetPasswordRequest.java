package com.capstone.pethouse.domain.User.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank String memberId,
        @NotBlank String memberName,
        @NotBlank String memberPhone,
        @NotBlank String newPassword
) {
}
