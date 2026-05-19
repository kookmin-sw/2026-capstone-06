package com.capstone.pethouse.domain.fan.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FanControlRequest(
        @JsonProperty("isRunning")
        @NotNull(message = "작동 여부는 필수값입니다.")
        Boolean isRunning,

        @Min(value = 1, message = "강도는 1 이상이어야 합니다.")
        @Max(value = 100, message = "강도는 100 이하여야 합니다.")
        Integer intensity
) {
}
