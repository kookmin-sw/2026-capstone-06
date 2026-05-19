package com.capstone.pethouse.domain.device.dto;

import com.capstone.pethouse.domain.device.entity.Device;
import com.fasterxml.jackson.annotation.JsonProperty;

public record DevicePopupResponse(
                @JsonProperty("member_id") String memberId,
                @JsonProperty("device_id") String deviceId) {

        public static DevicePopupResponse from(Device device) {
                return new DevicePopupResponse(
                                device.getUser().getMemberId(),
                                device.getDeviceId());
        }
}
