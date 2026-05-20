package com.capstone.pethouse.domain.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ActivityResponse {
    private String type; // FAN, WATER, FOOD
    private String message;
    private LocalDateTime timestamp;
    private String details;
}
