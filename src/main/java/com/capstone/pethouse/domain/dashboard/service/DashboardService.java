package com.capstone.pethouse.domain.dashboard.service;

import com.capstone.pethouse.domain.code.repository.CodeRepository;
import com.capstone.pethouse.domain.dashboard.dto.DashboardRequest.DeviceCreateReq;
import com.capstone.pethouse.domain.dashboard.dto.DashboardRequest.DeviceUpdateReq;
import com.capstone.pethouse.domain.dashboard.dto.DashboardResponse.*;
import com.capstone.pethouse.domain.dashboard.repository.DashboardSensorRepository;
import com.capstone.pethouse.domain.device.entity.Device;
import com.capstone.pethouse.domain.device.repository.DeviceRepository;
import com.capstone.pethouse.domain.serial.entity.Serial;
import com.capstone.pethouse.domain.serial.repository.SerialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DashboardService {

    private final DeviceRepository deviceRepository;
    private final SerialRepository serialRepository;
    private final CodeRepository codeRepository;
    private final DashboardSensorRepository sensorRepository;

    @Transactional(readOnly = true)
    public SensorDataRes getLatestSensorData(String deviceId) {
        SensorDataRes raw = sensorRepository.getLatestSensorData(deviceId);
        
        Optional<Device> deviceOpt = deviceRepository.findAll().stream().filter(d -> d.getDeviceId().equals(deviceId)).findFirst();

        // Enrich with Device data
        if (deviceOpt.isPresent()) {
            Device d = deviceOpt.get();
            // Create a full response
            return new SensorDataRes(
                    deviceId,
                    raw != null ? raw.temperature() : null,
                    raw != null ? raw.humidity() : null,
                    raw != null ? raw.heartRate() : null,
                    raw != null ? raw.co2() : null,
                    raw != null ? raw.lastUpdate() : null,
                    d.getSerialNum(),
                    d.getMemberId(),
                    d.getObjectCode(),
                    d.getObjectBirth() != null ? d.getObjectBirth().toString() : null,
                    d.getDeviceType(),
                    d.getDeviceTypeName(),
                    null, // memberName (We don't have member repo injected, so null is fine as per spec)
                    null, // memberPhone
                    null, // roleCode
                    d.isUse() // serialValid -> assuming isUse maps to it
            );
        }

        return raw; 
    }

    @Transactional(readOnly = true)
    public List<DeviceRes> getMemberDevices(String memberId) {
        // Find by memberId. I'll use simple filter since DeviceRepository might not have findByMemberId explicitly
        return deviceRepository.findAll().stream()
                .filter(d -> d.getMemberId().equals(memberId))
                .map(DeviceRes::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SensorHistoryRes> getHistory(String deviceId) {
        return sensorRepository.getSensorDataHistory(deviceId);
    }

    @Transactional
    public void upsertDevice(DeviceCreateReq dto) {
        if (dto.seq() != null && deviceRepository.existsById(dto.seq())) {
            // Update
            Device device = deviceRepository.findById(dto.seq()).orElseThrow();
            updateSerialState(device.getSerialNum(), dto.serialNum());
            device.update(dto.deviceId(), dto.memberId(), dto.serialNum(), dto.objectCode(), dto.objectBirth(), dto.deviceType());
        } else {
            // Insert
            Device device = Device.of(dto.deviceId(), dto.memberId(), dto.serialNum(), dto.objectCode(), dto.objectBirth(), dto.deviceType());
            deviceRepository.save(device);
            markSerialAsUsed(dto.serialNum());
        }
    }

    @Transactional
    public void updateDevice(String deviceId, DeviceUpdateReq dto) {
        Device device = deviceRepository.findAll().stream()
                .filter(d -> d.getDeviceId().equals(deviceId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("장치를 찾을 수 없습니다."));

        if (dto.serialNum() != null && !device.getSerialNum().equals(dto.serialNum())) {
            updateSerialState(device.getSerialNum(), dto.serialNum());
        }
        
        device.update(deviceId, 
                dto.memberId() != null ? dto.memberId() : device.getMemberId(),
                dto.serialNum() != null ? dto.serialNum() : device.getSerialNum(),
                dto.objectCode() != null ? dto.objectCode() : device.getObjectCode(),
                dto.objectBirth() != null ? dto.objectBirth() : device.getObjectBirth(),
                dto.deviceType() != null ? dto.deviceType() : device.getDeviceType()
        );
    }

    @Transactional
    public void deleteDevice(String deviceId) {
        Device device = deviceRepository.findAll().stream()
                .filter(d -> d.getDeviceId().equals(deviceId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("장치를 찾을 수 없습니다."));
                
        // Mark serial as unused
        markSerialAsUnused(device.getSerialNum());
        deviceRepository.delete(device);
    }

    @Transactional(readOnly = true)
    public StatusRes checkSerial(String serialNum) {
        Optional<Serial> serialOpt = serialRepository.findBySerialNum(serialNum);
        if (serialOpt.isEmpty()) {
            return new StatusRes("not_exist");
        }
        if (serialOpt.get().isUse()) {
            return new StatusRes("in_use");
        }
        return new StatusRes("ok");
    }

    @Transactional(readOnly = true)
    public DeviceRes getDeviceDetail(String deviceId) {
        Device device = deviceRepository.findAll().stream()
                .filter(d -> d.getDeviceId().equals(deviceId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("장치를 찾을 수 없습니다."));
        return DeviceRes.from(device);
    }

    @Transactional(readOnly = true)
    public List<CodeRes> getCodes() {
        return codeRepository.findAll().stream()
                .map(c -> new CodeRes(c.getCode(), c.getCodeName(), c.getGroupCode()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DashboardInitRes getDashboardInit(String memberId) {
        List<DeviceRes> devices = getMemberDevices(memberId);
        
        String selectedDeviceId = !devices.isEmpty() ? devices.get(0).deviceId() : null;
        SensorDataRes latestData = selectedDeviceId != null ? getLatestSensorData(selectedDeviceId) : null;
        
        return new DashboardInitRes(devices, selectedDeviceId, latestData);
    }

    private void updateSerialState(String oldSerial, String newSerial) {
        if (!oldSerial.equals(newSerial)) {
            markSerialAsUnused(oldSerial);
            markSerialAsUsed(newSerial);
        }
    }

    private void markSerialAsUsed(String serialNum) {
        serialRepository.findBySerialNum(serialNum).ifPresent(Serial::markUsed);
    }

    private void markSerialAsUnused(String serialNum) {
        serialRepository.findBySerialNum(serialNum).ifPresent(Serial::markUnused);
    }
}
