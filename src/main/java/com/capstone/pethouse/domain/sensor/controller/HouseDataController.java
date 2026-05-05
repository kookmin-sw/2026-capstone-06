package com.capstone.pethouse.domain.sensor.controller;

import com.capstone.pethouse.domain.sensor.dto.DataVo;
import com.capstone.pethouse.domain.sensor.dto.HouseDataRequest;
import com.capstone.pethouse.domain.sensor.service.HouseDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/data/house")
@RestController
public class HouseDataController {

    private final HouseDataService houseDataService;

    @GetMapping("/list")
    public ResponseEntity<Page<DataVo>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "15") int pageSize,
            @RequestParam(required = false) String searchQuery) {
        return ResponseEntity.ok(houseDataService.getList(pageNum, pageSize, searchQuery));
    }

    @GetMapping("/{seq}")
    public ResponseEntity<DataVo> get(@PathVariable Long seq) {
        return ResponseEntity.ok(houseDataService.get(seq));
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody HouseDataRequest request) {
        houseDataService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("등록 및 알림 처리 완료");
    }

    @PutMapping("/{seq}")
    public ResponseEntity<String> update(@PathVariable Long seq, @RequestBody HouseDataRequest request) {
        try {
            houseDataService.update(seq, request);
            return ResponseEntity.ok("수정되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("수정 실패");
        }
    }

    @DeleteMapping("/{seq}")
    public ResponseEntity<String> delete(@PathVariable Long seq) {
        houseDataService.delete(seq);
        return ResponseEntity.ok("삭제되었습니다.");
    }
}
