package com.capstone.pethouse.domain.device.dto;

import com.capstone.pethouse.domain.device.entity.Device;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.format.DateTimeFormatter;

public record DeviceVo(
        Long seq,
        String deviceId,
        String memberId,
        String serialNum,
        String objectCode,
        String objectName,
        String deviceType,
        String deviceTypeName,
        String objectBirth,
        @JsonProperty("isUse") boolean isUse,
        String regDate
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static DeviceVo from(Device device) {
        return new DeviceVo(
                device.getSeq(),
                device.getDeviceId(),
                device.getMemberId(),
                device.getSerialNum(),
                device.getObjectCode(),
                device.getObjectName(),
                device.getDeviceType(),
                device.getDeviceTypeName(),
                device.getObjectBirth() != null ? device.getObjectBirth().format(DATE_FORMATTER) : null,
                device.isUse(),
                device.getRegDate().format(DATETIME_FORMATTER)
        );
    }
}
