package com.capstone.pethouse.domain.device.controller;

import com.capstone.pethouse.domain.code.dto.CodeResponse;
import com.capstone.pethouse.domain.code.service.CodeService;
import com.capstone.pethouse.domain.device.dto.DevicePopupResponse;
import com.capstone.pethouse.domain.device.dto.DeviceRequest;
import com.capstone.pethouse.domain.device.dto.DeviceResponse;
import com.capstone.pethouse.domain.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/device")
@RestController
public class DeviceController {

    private final DeviceService deviceService;
    private final CodeService codeService;

    @GetMapping("/list")
    public ResponseEntity<Page<DeviceResponse>> list(
            @PageableDefault(size = 15, sort = "regDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String searchQuery) {
        return ResponseEntity.ok(deviceService.getDevices(searchType, searchQuery, pageable));
    }

    @GetMapping("/{seq}")
    public ResponseEntity<DeviceResponse> getDevice(@PathVariable Long seq) {
        return ResponseEntity.ok(deviceService.getDevice(seq));
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(@RequestBody DeviceRequest request) {
        DeviceResponse response = deviceService.createDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<DeviceResponse> updateDevice(@RequestBody DeviceRequest request) {
        DeviceResponse response = deviceService.updateDevice(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{seq}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long seq) {
        deviceService.deleteDevice(seq);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/popupList")
    public ResponseEntity<List<DevicePopupResponse>> getPopupList() {
        return ResponseEntity.ok(deviceService.getPopupList());
    }

    @GetMapping("/popupListByType")
    public ResponseEntity<List<DevicePopupResponse>> getPopupListByType(@RequestParam String deviceType) {
        return ResponseEntity.ok(deviceService.getPopupListByType(deviceType));
    }

    @GetMapping("/checkMember")
    public ResponseEntity<Map<String, String>> checkMember(@RequestParam String memberId) {
        return ResponseEntity.ok(deviceService.checkMember(memberId));
    }

    @GetMapping("/checkSerial")
    public ResponseEntity<Map<String, String>> checkSerial(@RequestParam String serialNum) {
        return ResponseEntity.ok(deviceService.checkSerial(serialNum));
    }

    @GetMapping("/deviceTypeCodes")
    public ResponseEntity<List<CodeResponse>> getDeviceTypeCodes() {
        return ResponseEntity.ok(codeService.getCodesByGroupCode("dtype"));
    }
}
