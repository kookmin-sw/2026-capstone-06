package com.capstone.pethouse.domain.fan.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FanControlResponse(
        Long houseId,
        @JsonProperty("isRunning")
        Boolean isRunning,
        Integer intensity
) {
    public static FanControlResponse of(Long houseId, Boolean isRunning, Integer intensity) {
        return new FanControlResponse(houseId, isRunning, intensity);
    }
}
