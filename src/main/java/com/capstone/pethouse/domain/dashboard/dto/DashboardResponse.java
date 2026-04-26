package com.capstone.pethouse.domain.dashboard.dto;

import com.capstone.pethouse.domain.device.entity.Device;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public class DashboardResponse {

    public record MessageRes(String message) {}
    
    public record StatusRes(String status) {}

    public record DeviceRes(
            Long seq,
            String deviceId,
            String memberId,
            String serialNum,
            String objectCode,
            String objectName,
            String deviceType,
            String deviceTypeName,
            String objectBirth,
            boolean isUse,
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime regDate
    ) {
        public static DeviceRes from(Device device) {
            return new DeviceRes(
                    device.getSeq(),
                    device.getDeviceId(),
                    device.getMemberId(),
                    device.getSerialNum(),
                    device.getObjectCode(),
                    device.getObjectName(),
                    device.getDeviceType(),
                    device.getDeviceTypeName(),
                    device.getObjectBirth() != null ? device.getObjectBirth().toString() : null,
                    device.isUse(),
                    device.getRegDate()
            );
        }
    }

    public record SensorDataRes(
            String deviceId,
            Double temperature,
            Double humidity,
            Double heartRate,
            Double co2,
            String lastUpdate,
            String serialNum,
            String memberId,
            String objectCode,
            String objectBirth,
            String deviceType,
            String deviceTypeName,
            String memberName,
            String memberPhone,
            String roleCode,
            Boolean serialValid
    ) {}

    public record SensorHistoryRes(
            Long seq,
            String deviceId,
            Double temVal,
            Double humVal,
            Double heartVal,
            Double coVal,
            String regDate
    ) {}

    public record CodeRes(
            String code,
            String codeName,
            String groupCode
    ) {}

    public record DashboardInitRes(
            List<DeviceRes> devices,
            String selectedDeviceId,
            SensorDataRes latestData
    ) {}
}
