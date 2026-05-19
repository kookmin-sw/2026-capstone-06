package com.capstone.pethouse.domain.fan.dto.response;

import com.capstone.pethouse.domain.fan.entity.FanSchedule;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public record FanScheduleResponse(
        Long houseId,
        Long scheduleId,
        List<FanScheduleDetailResponse> fanScheduleDetailResponseList,
        LocalTime startTime,
        LocalTime endTime
) {

    public static FanScheduleResponse from(FanSchedule fanSchedule) {
        List<FanScheduleDetailResponse> fanScheduleDetailResponseList =
                Optional.ofNullable(fanSchedule.getFanScheduleDetailSet())
                        .map(set -> set.stream().map(FanScheduleDetailResponse::from).toList())
                        .orElse(List.of());

        return new FanScheduleResponse(
                fanSchedule.getPetHouse().getHouseId(),
                fanSchedule.getId(),
                fanScheduleDetailResponseList,
                fanSchedule.getStartTime(),
                fanSchedule.getEndTime()
        );
    }
}
