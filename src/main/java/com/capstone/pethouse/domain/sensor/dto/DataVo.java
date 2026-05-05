package com.capstone.pethouse.domain.sensor.dto;

import com.capstone.pethouse.domain.sensor.entity.HouseData;
import com.capstone.pethouse.domain.sensor.entity.NeckData;

import java.time.format.DateTimeFormatter;

public record DataVo(
        Long seq,
        String deviceId,
        Double temVal,
        Double humVal,
        Double heartVal,
        Double coVal,
        String regDate
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static DataVo fromHouse(HouseData h) {
        return new DataVo(
                h.getSeq(),
                h.getDeviceId(),
                h.getTemVal(),
                h.getHumVal(),
                null,
                h.getCoVal(),
                h.getRegDate().format(FORMATTER)
        );
    }

    public static DataVo fromNeck(NeckData n) {
        return new DataVo(
                n.getSeq(),
                n.getDeviceId(),
                n.getTemVal(),
                null,
                n.getHeartVal(),
                n.getCoVal(),
                n.getRegDate().format(FORMATTER)
        );
    }
}
