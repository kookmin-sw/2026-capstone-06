package com.capstone.pethouse.domain.dashboard.controller;

import com.capstone.pethouse.domain.dashboard.dto.request.*;
import com.capstone.pethouse.domain.dashboard.dto.response.*;
import com.capstone.pethouse.domain.dashboard.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/dashboard")
@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/latest")
    public ResponseEntity<SensorDataResponse> getLatestSensorData(@RequestParam String deviceId) {
        return ResponseEntity.ok(dashboardService.getLatestSensorData(deviceId));
    }

    @GetMapping("/devices")
    public ResponseEntity<List<DeviceResponse>> getDevices(@RequestParam String memberId) {
        return ResponseEntity.ok(dashboardService.getMemberDevices(memberId));
    }

    @PostMapping("/device")
    public ResponseEntity<MessageResponse> createDevice(@Valid @RequestBody DeviceCreateRequest request) {
        dashboardService.createDevice(request);
        return ResponseEntity.ok(new MessageResponse("입력 완료"));
    }

    @PutMapping("/device/{deviceId}")
    public ResponseEntity<MessageResponse> updateDevice(@PathVariable String deviceId, @RequestBody DeviceUpdateRequest request) {
        dashboardService.updateDevice(deviceId, request);
        return ResponseEntity.ok(new MessageResponse("수정 완료"));
    }

    @DeleteMapping("/device/{deviceId}")
    public ResponseEntity<MessageResponse> deleteDevice(@PathVariable String deviceId) {
        dashboardService.deleteDevice(deviceId);
        return ResponseEntity.ok(new MessageResponse("삭제 완료"));
    }

    @GetMapping("/device/checkSerial")
    public ResponseEntity<StatusResponse> checkSerial(@RequestParam String serialNum) {
        return ResponseEntity.ok(dashboardService.checkSerial(serialNum));
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<DeviceResponse> getDeviceDetail(@PathVariable String deviceId) {
        return ResponseEntity.ok(dashboardService.getDeviceDetail(deviceId));
    }

    @GetMapping("/device/codes")
    public ResponseEntity<List<CodeResponse>> getCodes() {
        return ResponseEntity.ok(dashboardService.getCodes());
    }

    @GetMapping("/init")
    public ResponseEntity<DashboardInitResponse> getDashboardInit(@RequestParam String memberId) {
        return ResponseEntity.ok(dashboardService.getDashboardInit(memberId));
    }

    @GetMapping("/activities")
    public ResponseEntity<List<ActivityResponse>> getActivities(@RequestParam String deviceId) {
        return ResponseEntity.ok(dashboardService.getActivities(deviceId));
    }

    @GetMapping("/stats")
    public ResponseEntity<DailyStatsResponse> getDailyStats(@RequestParam String deviceId) {
        return ResponseEntity.ok(dashboardService.getDailyStats(deviceId));
    }
}
