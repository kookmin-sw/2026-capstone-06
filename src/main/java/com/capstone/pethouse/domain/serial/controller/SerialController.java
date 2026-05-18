package com.capstone.pethouse.domain.serial.controller;

import com.capstone.pethouse.domain.serial.dto.SerialRequest;
import com.capstone.pethouse.domain.serial.dto.SerialResponse;
import com.capstone.pethouse.domain.serial.service.SerialService;
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
@RequestMapping("/serial")
@RestController
public class SerialController {

    private final SerialService serialService;

    @GetMapping("/list")
    public ResponseEntity<Page<SerialResponse>> list(
            @PageableDefault(size = 15, sort = "regDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String searchQuery) {
        return ResponseEntity.ok(serialService.getSerials(searchQuery, pageable));
    }

    @GetMapping("/{seq}")
    public ResponseEntity<SerialResponse> getSerial(@PathVariable Long seq) {
        return ResponseEntity.ok(serialService.getSerial(seq));
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addSerial(@RequestBody SerialRequest request) {
        String message = serialService.addSerial(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", message));
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updateSerial(@RequestBody SerialRequest request) {
        String message = serialService.updateSerial(request);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @DeleteMapping("/delete/{seq}")
    public ResponseEntity<Map<String, String>> deleteSerial(@PathVariable Long seq) {
        String message = serialService.deleteSerial(seq);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PutMapping("/use/{serialNum}")
    public ResponseEntity<Map<String, String>> markAsUsed(@PathVariable String serialNum) {
        String message = serialService.markAsUsed(serialNum);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PutMapping("/release/{serialNum}")
    public ResponseEntity<Map<String, String>> markAsUnused(@PathVariable String serialNum) {
        String message = serialService.markAsUnused(serialNum);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/generate")
    public ResponseEntity<List<SerialResponse>> generateSerials(@RequestParam int count) {
        List<SerialResponse> result = serialService.generateSerials(count);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
