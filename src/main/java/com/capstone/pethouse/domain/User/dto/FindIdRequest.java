package com.capstone.pethouse.domain.User.dto;

import jakarta.validation.constraints.NotBlank;

public record FindIdRequest(
        @NotBlank String memberName,
        @NotBlank String memberPhone
) {
}
