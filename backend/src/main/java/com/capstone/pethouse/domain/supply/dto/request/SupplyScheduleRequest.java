package com.capstone.pethouse.domain.supply.dto.request;

import java.math.BigDecimal;

import com.capstone.pethouse.domain.enums.FeedType;
import com.capstone.pethouse.domain.enums.UnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SupplyScheduleRequest(

        @NotNull(message = "급여 종류는 필수입니다.")
        FeedType feedType,

        @NotNull(message = "단위는 필수입니다.")
        UnitType unitType,

        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 0보다 커야 합니다.")
        BigDecimal amount,

        @NotNull
        @NotBlank(message = "스케줄 시간은 필수입니다.")         // null, "", "  " 모두 방지
        String cronExpression,

        boolean enabled
) {
    
}
