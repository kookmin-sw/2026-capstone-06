package com.capstone.pethouse.domain.dashboard.dto.response;

import com.capstone.pethouse.domain.device.entity.Device;
import com.capstone.pethouse.domain.device.entity.PetHouse;
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
        LocalDateTime regDate,
        String nickname,
        String objectCode,
        String objectName,
        String objectBirth
) {
    public static DeviceResponse from(Device device) {
        PetHouse petHouse = device.getPetHouse();
        return new DeviceResponse(
                device.getSeq(),
                device.getDeviceId(),
                device.getUser() != null ? device.getUser().getMemberId() : null,
                device.getSerialNum(),
                device.getDeviceType(),
                device.isUse(),
                device.getRegDate(),
                petHouse != null ? petHouse.getNickname() : null,
                (petHouse != null && petHouse.getObjectCode() != null) ? petHouse.getObjectCode().getCode() : null,
                petHouse != null ? petHouse.getObjectName() : null,
                (petHouse != null && petHouse.getObjectBirth() != null) ? petHouse.getObjectBirth().toString() : null
        );
    }
}
