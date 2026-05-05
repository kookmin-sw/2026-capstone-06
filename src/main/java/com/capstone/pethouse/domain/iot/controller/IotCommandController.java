package com.capstone.pethouse.domain.iot.controller;

import com.capstone.pethouse.domain.iot.dto.CommandFetchRequest;
import com.capstone.pethouse.domain.iot.dto.CommandFetchResponse;
import com.capstone.pethouse.domain.iot.service.DeviceCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 디바이스 명세 v0.3 - POST /api/command/fetch
 * IoT 디바이스가 자기 SN의 대기(W) 명령을 폴링.
 */
@RequiredArgsConstructor
@RequestMapping("/command")
@RestController
public class IotCommandController {

    private final DeviceCommandService deviceCommandService;

    @PostMapping("/fetch")
    public ResponseEntity<List<CommandFetchResponse>> fetch(@RequestBody CommandFetchRequest request) {
        return ResponseEntity.ok(deviceCommandService.fetch(request.sn()));
    }
}
