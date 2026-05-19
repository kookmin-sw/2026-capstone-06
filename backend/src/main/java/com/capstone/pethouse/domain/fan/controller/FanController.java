package com.capstone.pethouse.domain.fan.controller;

import com.capstone.pethouse.domain.fan.dto.request.FanControlRequest;
import com.capstone.pethouse.domain.fan.dto.request.FanScheduleRequest;
import com.capstone.pethouse.domain.fan.dto.response.FanAutoModeResponse;
import com.capstone.pethouse.domain.fan.dto.response.FanControlResponse;
import com.capstone.pethouse.domain.fan.dto.response.FanHistoryResponse;
import com.capstone.pethouse.domain.fan.dto.response.FanScheduleResponse;
import com.capstone.pethouse.domain.fan.dto.response.FanStatsResponse;
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

    // 수동 환풍기 제어
    @PostMapping("/{houseId}/fan/control")
    public FanControlResponse controlFan(
            @PathVariable Long houseId,
            @Valid @RequestBody FanControlRequest request
    ) {
        return fanService.controlFan(houseId, request);
    }

    // 환풍기 전체 자동 모드 설정
    @PatchMapping("/{houseId}/fan/auto-mode")
    public FanAutoModeResponse toggleFanAutoMode(
            @PathVariable Long houseId,
            @RequestParam boolean isAutoMode
    ) {
        return fanService.toggleFanAutoMode(houseId, isAutoMode);
    }

    // 환풍기 최근 작동 이력 조회
    @GetMapping("/{houseId}/fan/history")
    public Page<FanHistoryResponse> getFanHistory(
            @PathVariable Long houseId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return fanService.getFanHistory(houseId, pageable);
    }

    // 환풍기 오늘 작동 통계 조회
    @GetMapping("/{houseId}/fan/statistics")
    public FanStatsResponse getFanStatistics(
            @PathVariable Long houseId
    ) {
        return fanService.getFanStatistics(houseId);
    }
}
