package com.capstone.pethouse.domain.fan.dto.response;

import com.capstone.pethouse.domain.fan.entity.FanScheduleDetail;

import java.math.BigDecimal;

public record FanScheduleDetailResponse(
        BigDecimal temperature,
        Integer speed
) {

    public static FanScheduleDetailResponse from(FanScheduleDetail fanScheduleDetail) {
        return new FanScheduleDetailResponse(
                fanScheduleDetail.getTemperature(),
                fanScheduleDetail.getSpeed()
        );
    }
}
