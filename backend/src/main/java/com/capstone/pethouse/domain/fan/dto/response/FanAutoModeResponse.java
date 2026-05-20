package com.capstone.pethouse.domain.fan.dto.response;

public record FanAutoModeResponse(
        Long houseId,
        Boolean isAutoMode
) {
    public static FanAutoModeResponse of(Long houseId, Boolean isAutoMode) {
        return new FanAutoModeResponse(houseId, isAutoMode);
    }
}
