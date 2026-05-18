package com.capstone.pethouse.domain.dashboard.dto.response;

public record SensorDataResponse(
        String deviceId,
        Double temperature,
        Double humidity,
        Double heartRate,
        Double co2,
        String lastUpdate
) {}
