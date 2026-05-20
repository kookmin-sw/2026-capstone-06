package com.capstone.pethouse.domain.fan.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

public record FanScheduleRequest(

        @Valid
        @NotNull(message = "상세 설정은 최소 하나 이상 필요합니다.")
        List<FanScheduleDetailRequest> fanScheduleDetailRequestList,

        @NotNull(message = "시작 시간은 필수입니다.")
        LocalTime startTime,

        @NotNull(message = "종료 시간은 필수입니다.")
        LocalTime endTime,

        boolean enabled
) {

}