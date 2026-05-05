package com.capstone.pethouse.domain.sensor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HouseDataRequest(
        @JsonProperty("device_id") String deviceId,
        @JsonProperty("tem_val") Double temVal,
        @JsonProperty("hum_val") Double humVal,
        @JsonProperty("co_val") Double coVal
) {
}
