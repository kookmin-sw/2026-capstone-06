package com.capstone.pethouse.domain.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeviceRequest(
        Long seq,
        @JsonProperty("device_id") String deviceId,
        @JsonProperty("member_id") String memberId,
        @JsonProperty("serial_num") String serialNum,
        @JsonProperty("old_serial_num") String oldSerialNum,
        @JsonProperty("object_code") String objectCode,
        @JsonProperty("object_birth") String objectBirth,
        @JsonProperty("device_type") String deviceType
) {
}
