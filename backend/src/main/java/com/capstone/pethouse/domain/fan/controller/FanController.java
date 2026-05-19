package com.capstone.pethouse.domain.fan.controller;

import com.capstone.pethouse.domain.fan.dto.request.FanScheduleRequest;
import com.capstone.pethouse.domain.fan.dto.response.FanScheduleResponse;
import com.capstone.pethouse.domain.fan.dto.response.FanToggleResponse;
import com.capstone.pethouse.domain.fan.service.FanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/devices")
@RestController
public class FanController {

    private final FanService fanService;

    // 자동 FAN 스케줄러 가져오기
    @GetMapping("/{houseId}/fan/schedules")
    public Page<FanScheduleResponse> getFanSchedules(
            @PathVariable Long houseId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return fanService.getFanSchedules(houseId, pageable);
    }

    // 자동 FAN 스케줄러 등록
    @PostMapping("/{houseId}/fan/schedules")
    public FanScheduleResponse postSupplySchedule(
            @PathVariable Long houseId,
            @Valid @RequestBody FanScheduleRequest fanScheduleRequest
    ) {
        return fanService.postFanSchedule(houseId, fanScheduleRequest);
    }

    // 자동 FAN 스케줄러 수정
    @PutMapping("/{houseId}/fan/schedules/{scheduleId}")
    public FanScheduleResponse updateSupplySchedule(
            @PathVariable Long houseId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody FanScheduleRequest fanScheduleRequest
    ) {
        return fanService.updateFanSchedule(houseId, scheduleId, fanScheduleRequest);
    }

    // 자동 FAN 스케줄러 활성, 비활성 토글
    @PatchMapping("/{houseId}/fan/schedules/{scheduleId}/toggle")
    public FanToggleResponse toggleFanSchedule(
            @PathVariable Long houseId,
            @PathVariable Long scheduleId,
            @RequestParam boolean enabled
    ) {
        return fanService.toggleFanSchedule(houseId, scheduleId, enabled);
    }

    // 자동 Fan 스케줄러 삭제
    @DeleteMapping("/{houseId}/fan/schedules/{scheduleId}")
    public Long deleteFanSchedule(
            @PathVariable Long houseId,
            @PathVariable Long scheduleId
    ) {
        return fanService.deleteFanSchedule(houseId, scheduleId);
    }
}
