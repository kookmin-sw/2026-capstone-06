package com.capstone.pethouse.domain.sensor.dto;

public record SensorResponse(
        Long seq,
        String deviceId,
        Double temVal,
        Double humVal,
        Double heartVal,
        Double coVal,
        String regDate
) {
}
