package com.capstone.pethouse.domain.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DevicePopupResponse(
        @JsonProperty("member_id") String memberId,
        @JsonProperty("device_id") String deviceId
) {
}
