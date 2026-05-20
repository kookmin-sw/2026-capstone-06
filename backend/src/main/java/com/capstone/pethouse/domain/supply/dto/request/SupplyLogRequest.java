package com.capstone.pethouse.domain.supply.dto.request;

import com.capstone.pethouse.domain.enums.FeedType;
import com.capstone.pethouse.domain.enums.TriggerType;
import com.capstone.pethouse.domain.enums.UnitType;

import java.math.BigDecimal;

public record SupplyLogRequest(
        Long scheduleId,
        FeedType feedType,
        UnitType unitType,
        BigDecimal amount,
        TriggerType triggerType             // 자동 / 수동
) {
}
