package com.capstone.pethouse.domain.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DashboardRequest {

    public record DeviceCreateReq(
            @NotNull Long houseId,
            @NotBlank String deviceId,
            @NotBlank String memberId,
            @NotBlank String serialNum,
            @NotBlank String deviceType
    ) {}

    public record DeviceUpdateReq(
            String memberId,
            String serialNum,
            String deviceType
    ) {}

}
