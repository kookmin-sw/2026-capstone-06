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
            String deviceType,
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
                    device.getDeviceType(),
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
            String lastUpdate
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
