package com.capstone.pethouse.domain.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DailyStatsResponse {
    private long fanRunCount;
    private long waterSupplyCount;
    private BigDecimal waterSupplyAmount;
    private long foodSupplyCount;
    private BigDecimal foodSupplyAmount;
}
