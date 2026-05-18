package com.capstone.pethouse.domain.sensor.dto;

import com.capstone.pethouse.domain.sensor.entity.HouseData;

import java.time.format.DateTimeFormatter;

public record SensorResponse(
        Long seq,
        String deviceId,
        Double temVal,
        Double humVal,
        Double heartVal,
        Double coVal,
        String regDate) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static SensorResponse fromHouse(HouseData h) {
        return new SensorResponse(
                h.getSeq(),
                h.getDeviceId(),
                h.getTemVal(),
                h.getHumVal(),
                null,
                h.getCoVal(),
                h.getRegDate().format(FORMATTER));
    }

}
