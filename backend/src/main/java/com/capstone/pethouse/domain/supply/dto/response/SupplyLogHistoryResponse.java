package com.capstone.pethouse.domain.supply.dto.response;

import com.capstone.pethouse.domain.enums.ExecutionStatus;
import com.capstone.pethouse.domain.enums.FeedType;
import com.capstone.pethouse.domain.enums.TriggerType;
import com.capstone.pethouse.domain.enums.UnitType;
import com.capstone.pethouse.domain.supply.entity.SupplyLog;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SupplyLogHistoryResponse(
        Long houseId,
        Long scheduleId,            // 수동 제공 시 null 값 가능
        FeedType feedType,
        UnitType unitType,
        BigDecimal amount,
        TriggerType triggerType,
        ExecutionStatus executionStatus,
        LocalDateTime createdAt
) {

    public static SupplyLogHistoryResponse from(SupplyLog supplyLog) {
        return new SupplyLogHistoryResponse(
                supplyLog.getPetHouse().getHouseId(),
                supplyLog.getSupplySchedule() != null ? supplyLog.getSupplySchedule().getId() : null,
                supplyLog.getFeedType(),
                supplyLog.getUnitType(),
                supplyLog.getAmount(),
                supplyLog.getTriggerType(),
                supplyLog.getExecutionStatus(),
                supplyLog.getCreatedAt()
        );
    }
}
