package com.capstone.pethouse.domain.supply.dto.response;

import com.capstone.pethouse.domain.enums.ExecutionStatus;
import com.capstone.pethouse.domain.enums.FeedType;
import com.capstone.pethouse.domain.enums.TriggerType;
import com.capstone.pethouse.domain.enums.UnitType;
import com.capstone.pethouse.domain.supply.entity.SupplyLog;

import java.math.BigDecimal;

public record SupplyLogResponse(
        Long houseId,
        FeedType feedType,
        UnitType unitType,
        BigDecimal amount,
        TriggerType triggerType,
        ExecutionStatus executionStatus
) {

    public static SupplyLogResponse from(SupplyLog supplyLog) {
        return new SupplyLogResponse(
                supplyLog.getPetHouse().getHouseId(),
                supplyLog.getFeedType(),
                supplyLog.getUnitType(),
                supplyLog.getAmount(),
                supplyLog.getTriggerType(),
                supplyLog.getExecutionStatus()
        );
    }
}
