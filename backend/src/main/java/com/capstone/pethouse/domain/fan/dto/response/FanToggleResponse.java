package com.capstone.pethouse.domain.fan.dto.response;

import com.capstone.pethouse.domain.fan.entity.FanSchedule;

public record FanToggleResponse(
        Long houseId,
        Long scheduleId,
        boolean enabled
) {

    public static FanToggleResponse from(FanSchedule fanSchedule) {
        return new FanToggleResponse(
                fanSchedule.getPetHouse().getHouseId(),
                fanSchedule.getId(),
                fanSchedule.isEnabled()
        );
    }
}
