package com.capstone.pethouse.domain.sensor.controller;

import com.capstone.pethouse.domain.sensor.dto.DataVo;
import com.capstone.pethouse.domain.sensor.service.ChartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/data/chart")
@RestController
public class ChartController {

    private final ChartService chartService;

    @GetMapping
    public ResponseEntity<List<DataVo>> getChart(@RequestParam String serialNum) {
        return ResponseEntity.ok(chartService.getChartData(serialNum));
    }
}
