package com.capstone.pethouse.domain.dashboard.service;

import com.capstone.pethouse.domain.code.repository.CodeRepository;
import com.capstone.pethouse.domain.dashboard.dto.request.*;
import com.capstone.pethouse.domain.dashboard.dto.response.*;
import com.capstone.pethouse.domain.dashboard.repository.DashboardSensorRepository;
import com.capstone.pethouse.domain.device.entity.Device;
import com.capstone.pethouse.domain.device.repository.DeviceRepository;
import com.capstone.pethouse.domain.User.entity.User;
import com.capstone.pethouse.domain.User.repository.UserRepository;
import com.capstone.pethouse.domain.device.entity.PetHouse;
import com.capstone.pethouse.domain.device.repository.PetHouseRepository;
import com.capstone.pethouse.domain.serial.entity.Serial;
import com.capstone.pethouse.domain.serial.repository.SerialRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private final UserRepository userRepository;
    private final PetHouseRepository petHouseRepository;

    @Transactional(readOnly = true)
    public SensorDataResponse getLatestSensorData(String deviceId) {
        return sensorRepository.getLatestSensorData(deviceId);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getMemberDevices(String memberId) {
        return deviceRepository.findByMemberId(memberId).stream()
                .map(DeviceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createDevice(DeviceCreateRequest dto) {
        User user = userRepository.findByMemberId(dto.memberId())
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        Serial serial = serialRepository.findBySerialNum(dto.serialNum())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시리얼 번호입니다."));

        if (serial.isUse()) {
            throw new IllegalArgumentException("이미 사용중인 시리얼 번호입니다.");
        }

        // 전달된 houseId로 펫하우스 조회
        PetHouse petHouse = petHouseRepository.findById(dto.houseId())
                .orElseThrow(() -> new EntityNotFoundException("펫하우스를 찾을 수 없습니다."));

        // 유저 소유 확인
        if (!petHouse.getUser().equals(user)) {
            throw new IllegalArgumentException("해당 펫하우스에 대한 권한이 없습니다.");
        }

        Device device = Device.of(dto.deviceId(), user, dto.serialNum(), dto.deviceType());
        device.assignToPetHouse(petHouse);
        
        deviceRepository.save(device);
        markSerialAsUsed(dto.serialNum());
    }

    @Transactional
    public void updateDevice(String deviceId, DeviceUpdateRequest dto) {
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("장치를 찾을 수 없습니다."));

        if (dto.serialNum() != null && !device.getSerialNum().equals(dto.serialNum())) {
            Serial serial = serialRepository.findBySerialNum(dto.serialNum())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시리얼 번호입니다."));
            
            if (serial.isUse()) {
                throw new IllegalArgumentException("이미 사용중인 시리얼 번호입니다.");
            }

            updateSerialState(device.getSerialNum(), dto.serialNum());
        }
        
        User user = device.getUser();
        if (dto.memberId() != null && (device.getUser() == null || !dto.memberId().equals(device.getUser().getMemberId()))) {
            user = userRepository.findByMemberId(dto.memberId())
                    .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        }

        device.update(deviceId,
                user,
                dto.serialNum() != null ? dto.serialNum() : device.getSerialNum(),
                dto.deviceType() != null ? dto.deviceType() : device.getDeviceType()
        );
    }

    @Transactional
    public void deleteDevice(String deviceId) {
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("장치를 찾을 수 없습니다."));
                
        // Mark serial as unused
        markSerialAsUnused(device.getSerialNum());
        deviceRepository.delete(device);
    }

    @Transactional(readOnly = true)
    public StatusResponse checkSerial(String serialNum) {
        Optional<Serial> serialOpt = serialRepository.findBySerialNum(serialNum);
        if (serialOpt.isEmpty()) {
            return new StatusResponse("not_exist");
        }
        if (serialOpt.get().isUse()) {
            return new StatusResponse("in_use");
        }
        return new StatusResponse("ok");
    }

    @Transactional(readOnly = true)
    public DeviceResponse getDeviceDetail(String deviceId) {
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("장치를 찾을 수 없습니다."));
        return DeviceResponse.from(device);
    }

    @Transactional(readOnly = true)
    public List<CodeResponse> getCodes() {
        return codeRepository.findAllWithParent().stream()
                .map(CodeResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DashboardInitResponse getDashboardInit(String memberId) {
        List<DeviceResponse> devices = getMemberDevices(memberId);
        
        String selectedDeviceId = !devices.isEmpty() ? devices.getFirst().deviceId() : null;
        SensorDataResponse latestData = selectedDeviceId != null ? getLatestSensorData(selectedDeviceId) : null;
        
        return new DashboardInitResponse(devices, selectedDeviceId, latestData);
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
