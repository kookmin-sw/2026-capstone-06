package com.capstone.pethouse.domain.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeviceRequest(
        Long seq,
        @JsonProperty("house_id") Long petHouseId,
        @JsonProperty("member_id") String memberId,
        @JsonProperty("serial_num") String serialNum,
        @JsonProperty("device_type") String deviceType,
        @JsonProperty("device_id") String deviceId,
        @JsonProperty("old_serial_num") String oldSerialNum
) {
}
