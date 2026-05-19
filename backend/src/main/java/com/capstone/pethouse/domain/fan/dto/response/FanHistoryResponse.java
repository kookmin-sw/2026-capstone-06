package com.capstone.pethouse.domain.fan.dto.response;

import com.capstone.pethouse.domain.enums.ExecutionStatus;
import com.capstone.pethouse.domain.enums.TriggerType;
import com.capstone.pethouse.domain.fan.entity.FanLog;

import java.time.Duration;
import java.time.LocalDateTime;

public record FanHistoryResponse(
        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long durationMinutes,
        Integer speed,
        TriggerType triggerType,
        ExecutionStatus executionStatus,
        LocalDateTime createdAt
) {
    public static FanHistoryResponse from(FanLog log) {
        long durationMin = 0;
        if (log.getStartTime() != null && log.getEndTime() != null) {
            durationMin = Duration.between(log.getStartTime(), log.getEndTime()).toMinutes();
        }
        return new FanHistoryResponse(
                log.getId(),
                log.getStartTime(),
                log.getEndTime(),
                durationMin,
                log.getSpeed(),
                log.getTriggerType(),
                log.getExecutionStatus(),
                log.getCreatedAt()
        );
    }
}
