package com.capstone.pethouse.domain.device.controller;

import com.capstone.pethouse.domain.code.dto.CodeVo;
import com.capstone.pethouse.domain.code.service.CodeService;
import com.capstone.pethouse.domain.device.dto.DeviceRequest;
import com.capstone.pethouse.domain.device.dto.DeviceVo;
import com.capstone.pethouse.domain.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<Page<DeviceVo>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "15") int pageSize,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String searchQuery) {
        return ResponseEntity.ok(deviceService.getDevices(pageNum, pageSize, searchType, searchQuery));
    }

    @GetMapping("/{seq}")
    public ResponseEntity<?> getDevice(@PathVariable Long seq) {
        try {
            return ResponseEntity.ok(deviceService.getDevice(seq));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createDevice(@RequestBody DeviceRequest request) {
        try {
            DeviceVo response = deviceService.createDevice(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<?> updateDevice(@RequestBody DeviceRequest request) {
        try {
            DeviceVo response = deviceService.updateDevice(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{seq}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long seq) {
        deviceService.deleteDevice(seq);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/popupList")
    public ResponseEntity<List<Map<String, Object>>> getPopupList() {
        return ResponseEntity.ok(deviceService.getPopupList());
    }

    @GetMapping("/popupListByType")
    public ResponseEntity<List<Map<String, Object>>> getPopupListByType(@RequestParam String deviceType) {
        return ResponseEntity.ok(deviceService.getPopupListByType(deviceType));
    }

    @GetMapping("/checkMember")
    public ResponseEntity<Map<String, String>> checkMember(@RequestParam("member_id") String memberId) {
        return ResponseEntity.ok(deviceService.checkMember(memberId));
    }

    @GetMapping("/checkSerial")
    public ResponseEntity<Map<String, String>> checkSerial(@RequestParam("serial_num") String serialNum) {
        return ResponseEntity.ok(deviceService.checkSerial(serialNum));
    }

    @GetMapping("/deviceTypeCodes")
    public ResponseEntity<List<CodeVo>> getDeviceTypeCodes() {
        return ResponseEntity.ok(codeService.getCodesByGroupCode("dtype"));
    }
}
