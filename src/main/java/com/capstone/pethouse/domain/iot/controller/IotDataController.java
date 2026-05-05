package com.capstone.pethouse.domain.iot.controller;

import com.capstone.pethouse.domain.iot.dto.IotDataRequest;
import com.capstone.pethouse.domain.iot.service.IotDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 디바이스 명세 v0.3 - POST /api/data
 * IoT 디바이스가 환경 데이터(SN/T/H/CO)를 직접 등록.
 */
@RequiredArgsConstructor
@RequestMapping("/data")
@RestController
public class IotDataController {

    private final IotDataService iotDataService;

    @PostMapping
    public ResponseEntity<String> register(@RequestBody IotDataRequest request) {
        iotDataService.registerEnvironmentData(request);
        return ResponseEntity.ok("등록 및 알림 처리 완료");
    }
}
