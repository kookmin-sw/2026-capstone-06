package com.capstone.pethouse.domain.fan.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FanScheduleDetailRequest(

        @NotNull(message = "기준 온도는 필수입니다.")
        BigDecimal temperature,

        @NotNull(message = "팬 강도는 필수입니다.")
        @Min(value = 0, message = "팬 강도는 0% 이상이어야 합니다.")
        @Max(value = 100, message = "팬 강도는 100% 이하여야 합니다.")
        Integer speed
) {

}