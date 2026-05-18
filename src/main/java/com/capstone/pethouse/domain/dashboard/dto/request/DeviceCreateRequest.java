package com.capstone.pethouse.domain.dashboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceCreateRequest(
        @NotNull Long houseId,
        @NotBlank String deviceId,
        @NotBlank String memberId,
        @NotBlank String serialNum,
        @NotBlank String deviceType
) {}
