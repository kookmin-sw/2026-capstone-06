package com.capstone.pethouse.domain.dashboard.dto.response;

import com.capstone.pethouse.domain.device.entity.Device;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record DeviceResponse(
        Long seq,
        String deviceId,
        String memberId,
        String serialNum,
        String deviceType,
        boolean isUse,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime regDate
) {
    public static DeviceResponse from(Device device) {
        return new DeviceResponse(
                device.getSeq(),
                device.getDeviceId(),
                device.getUser() != null ? device.getUser().getMemberId() : null,
                device.getSerialNum(),
                device.getDeviceType(),
                device.isUse(),
                device.getRegDate()
        );
    }
}
