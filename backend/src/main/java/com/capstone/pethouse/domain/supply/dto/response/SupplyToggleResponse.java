package com.capstone.pethouse.domain.supply.dto.response;

import com.capstone.pethouse.domain.supply.entity.SupplySchedule;

public record SupplyToggleResponse(
        Long houseId,
        Long scheduleId,
        boolean enabled
) {
    public static SupplyToggleResponse from(SupplySchedule supplySchedule) {
        return new SupplyToggleResponse(
                supplySchedule.getPetHouse().getHouseId(),
                supplySchedule.getId(),
                supplySchedule.isEnabled()
        );
    }
}
