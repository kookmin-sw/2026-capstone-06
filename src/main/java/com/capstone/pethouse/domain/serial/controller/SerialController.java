package com.capstone.pethouse.domain.serial.controller;

import com.capstone.pethouse.domain.serial.dto.SerialRequest;
import com.capstone.pethouse.domain.serial.dto.SerialVo;
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
    public ResponseEntity<Page<SerialVo>> list(
            @PageableDefault(size = 15, sort = "regDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String searchQuery) {
        return ResponseEntity.ok(serialService.getSerials(searchQuery, pageable));
    }

    @GetMapping("/{seq}")
    public ResponseEntity<SerialVo> getSerial(@PathVariable Long seq) {
        return ResponseEntity.ok(serialService.getSerial(seq));
    }

    @PostMapping("/add")
    public ResponseEntity<String> addSerial(@RequestBody SerialRequest request) {
        String message = serialService.addSerial(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateSerial(@RequestBody SerialRequest request) {
        String message = serialService.updateSerial(request);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/delete/{seq}")
    public ResponseEntity<String> deleteSerial(@PathVariable Long seq) {
        String message = serialService.deleteSerial(seq);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/use/{serialNum}")
    public ResponseEntity<String> markAsUsed(@PathVariable String serialNum) {
        String message = serialService.markAsUsed(serialNum);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/release/{serialNum}")
    public ResponseEntity<String> markAsUnused(@PathVariable String serialNum) {
        String message = serialService.markAsUnused(serialNum);
        return ResponseEntity.ok(message);
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
