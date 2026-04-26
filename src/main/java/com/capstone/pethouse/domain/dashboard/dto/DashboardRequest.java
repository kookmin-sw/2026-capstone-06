package com.capstone.pethouse.domain.dashboard.dto;

import java.time.LocalDate;

public class DashboardRequest {

    public record DeviceCreateReq(
            String deviceId,
            String memberId,
            String serialNum,
            String objectCode,
            LocalDate objectBirth,
            String deviceType,
            Long seq
    ) {}

    public record DeviceUpdateReq(
            String memberId,
            String serialNum,
            String objectCode,
            LocalDate objectBirth,
            String deviceType
    ) {}

}
