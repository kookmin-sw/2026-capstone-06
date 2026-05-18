package com.capstone.pethouse.domain.sensor.controller;

import com.capstone.pethouse.domain.sensor.dto.SensorResponse;
import com.capstone.pethouse.domain.sensor.service.ChartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/data")
@RestController
public class SensorController {

    private final ChartService chartService;

    @GetMapping("/chart")
    public ResponseEntity<List<SensorResponse>> getChart(
            @RequestParam String serialNum,
            @RequestParam(defaultValue = "-24h") String range,
            @RequestParam(defaultValue = "10m") String interval) {
        return ResponseEntity.ok(chartService.getChartData(serialNum, range, interval));
    }

}
