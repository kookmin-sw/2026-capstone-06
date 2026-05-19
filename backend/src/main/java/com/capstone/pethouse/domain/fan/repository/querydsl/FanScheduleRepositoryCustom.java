package com.capstone.pethouse.domain.fan.repository.querydsl;

import java.time.LocalTime;

public interface FanScheduleRepositoryCustom {
    boolean existingOverlappingSchedule(Long houseId, LocalTime startTime, LocalTime endTime);
    boolean existingOverlappingScheduleExcludingSelf(Long houseId, Long scheduleId, LocalTime startTime, LocalTime endTime);
}
