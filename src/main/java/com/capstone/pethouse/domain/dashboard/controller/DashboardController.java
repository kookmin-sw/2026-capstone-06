package com.capstone.pethouse.domain.dashboard.controller;

import com.capstone.pethouse.domain.dashboard.dto.DashboardRequest.DeviceCreateReq;
import com.capstone.pethouse.domain.dashboard.dto.DashboardRequest.DeviceUpdateReq;
import com.capstone.pethouse.domain.dashboard.dto.DashboardResponse.*;
import com.capstone.pethouse.domain.dashboard.service.DashboardService;
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
    public ResponseEntity<SensorDataRes> getLatestSensorData(@RequestParam String deviceId) {
        return ResponseEntity.ok(dashboardService.getLatestSensorData(deviceId));
    }

    @GetMapping("/devices")
    public ResponseEntity<List<DeviceRes>> getDevices(@RequestParam String memberId) {
        return ResponseEntity.ok(dashboardService.getMemberDevices(memberId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<SensorHistoryRes>> getHistory(@RequestParam String deviceId) {
        return ResponseEntity.ok(dashboardService.getHistory(deviceId));
    }

    @PostMapping("/device")
    public ResponseEntity<MessageRes> createDevice(@RequestBody DeviceCreateReq request) {
        if (request.deviceId() == null || request.memberId() == null ||
            request.serialNum() == null || request.deviceType() == null) {
            return ResponseEntity.badRequest().body(new MessageRes("필수 값 누락"));
        }
        
        try {
            dashboardService.upsertDevice(request);
            return ResponseEntity.ok(new MessageRes("입력 완료"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageRes(e.getMessage()));
        }
    }

    @PutMapping("/device/{deviceId}")
    public ResponseEntity<MessageRes> updateDevice(@PathVariable String deviceId, @RequestBody DeviceUpdateReq request) {
        dashboardService.updateDevice(deviceId, request);
        return ResponseEntity.ok(new MessageRes("수정 완료"));
    }

    @DeleteMapping("/device/{deviceId}")
    public ResponseEntity<MessageRes> deleteDevice(@PathVariable String deviceId) {
        dashboardService.deleteDevice(deviceId);
        return ResponseEntity.ok(new MessageRes("삭제 완료"));
    }

    @GetMapping("/device/checkSerial")
    public ResponseEntity<StatusRes> checkSerial(@RequestParam String serialNum) {
        return ResponseEntity.ok(dashboardService.checkSerial(serialNum));
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<DeviceRes> getDeviceDetail(@PathVariable String deviceId) {
        return ResponseEntity.ok(dashboardService.getDeviceDetail(deviceId));
    }

    @GetMapping("/device/codes")
    public ResponseEntity<List<CodeRes>> getCodes() {
        return ResponseEntity.ok(dashboardService.getCodes());
    }

    @GetMapping("/init")
    public ResponseEntity<DashboardInitRes> getDashboardInit(@RequestParam String memberId) {
        return ResponseEntity.ok(dashboardService.getDashboardInit(memberId));
    }
}
