package com.capstone.pethouse.domain.dashboard.dto.response;

import java.util.List;

public record DashboardInitResponse(
        List<DeviceResponse> devices,
        String selectedDeviceId,
        SensorDataResponse latestData
) {}
