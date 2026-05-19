package com.capstone.pethouse.domain.dashboard.dto.request;

public record DeviceUpdateRequest(
        String memberId,
        String serialNum,
        String deviceType
) {}
