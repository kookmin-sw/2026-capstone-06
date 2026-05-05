package com.capstone.pethouse.domain.sensor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NeckDataRequest(
        @JsonProperty("device_id") String deviceId,
        @JsonProperty("tem_val") Double temVal,
        @JsonProperty("heart_val") Double heartVal,
        @JsonProperty("co_val") Double coVal
) {
}
