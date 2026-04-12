package com.capstone.pethouse.domain.serial.controller;

import com.capstone.pethouse.domain.serial.dto.SerialRequest;
import com.capstone.pethouse.domain.serial.dto.SerialVo;
import com.capstone.pethouse.domain.serial.service.SerialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/serial")
@RestController
public class SerialController {

    private final SerialService serialService;

    @GetMapping("/list")
    public ResponseEntity<Page<SerialVo>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "15") int pageSize,
            @RequestParam(required = false) String searchQuery) {
        return ResponseEntity.ok(serialService.getSerials(pageNum, pageSize, searchQuery));
    }

    @GetMapping("/{seq}")
    public ResponseEntity<?> getSerial(@PathVariable Long seq) {
        try {
            return ResponseEntity.ok(serialService.getSerial(seq));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addSerial(@RequestBody SerialRequest request) {
        try {
            String message = serialService.addSerial(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateSerial(@RequestBody SerialRequest request) {
        try {
            String message = serialService.updateSerial(request);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("수정 실패: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{seq}")
    public ResponseEntity<?> deleteSerial(@PathVariable Long seq) {
        try {
            String message = serialService.deleteSerial(seq);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/use/{serialNum}")
    public ResponseEntity<?> markAsUsed(@PathVariable String serialNum) {
        try {
            String message = serialService.markAsUsed(serialNum);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/release/{serialNum}")
    public ResponseEntity<?> markAsUnused(@PathVariable String serialNum) {
        try {
            String message = serialService.markAsUnused(serialNum);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateSerials(@RequestParam int count) {
        try {
            List<SerialVo> result = serialService.generateSerials(count);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
