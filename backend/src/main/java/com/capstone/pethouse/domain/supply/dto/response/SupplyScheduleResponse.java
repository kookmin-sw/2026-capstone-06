package com.capstone.pethouse.domain.supply.dto.response;

import com.capstone.pethouse.domain.enums.FeedType;
import com.capstone.pethouse.domain.enums.UnitType;
import com.capstone.pethouse.domain.supply.entity.SupplySchedule;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SupplyScheduleResponse(
        Long houseId,
        Long scheduleId,
        FeedType feedType,
        UnitType unitType,
        BigDecimal amount,
        String cronExpression,
        boolean enabled,
        LocalDateTime lastRunAt
) {
    public static SupplyScheduleResponse from(SupplySchedule supplySchedule) {
        return new SupplyScheduleResponse(
                supplySchedule.getPetHouse().getHouseId(),
                supplySchedule.getId(),
                supplySchedule.getFeedType(), 
                supplySchedule.getUnitType(), 
                supplySchedule.getAmount(), 
                supplySchedule.getCronExpression(), 
                supplySchedule.isEnabled(),
                supplySchedule.getLastRunAt()
        );
    }
}
