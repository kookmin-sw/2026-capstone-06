package com.capstone.pethouse.domain.fan.service;

import com.capstone.pethouse.domain.fan.entity.FanLog;
import com.capstone.pethouse.domain.device.entity.PetHouse;
import com.capstone.pethouse.domain.device.repository.PetHouseRepository;
import com.capstone.pethouse.domain.fan.dto.request.FanControlRequest;
import com.capstone.pethouse.domain.fan.dto.request.FanScheduleDetailRequest;
import com.capstone.pethouse.domain.fan.dto.request.FanScheduleRequest;
import com.capstone.pethouse.domain.fan.dto.response.FanAutoModeResponse;
import com.capstone.pethouse.domain.fan.dto.response.FanControlResponse;
import com.capstone.pethouse.domain.fan.dto.response.FanHistoryResponse;
import com.capstone.pethouse.domain.fan.dto.response.FanScheduleResponse;
import com.capstone.pethouse.domain.fan.dto.response.FanStatsResponse;
import com.capstone.pethouse.domain.fan.dto.response.FanToggleResponse;
import com.capstone.pethouse.domain.fan.entity.FanSchedule;
import com.capstone.pethouse.domain.fan.repository.FanLogRepository;
import com.capstone.pethouse.domain.fan.repository.FanScheduleRepository;
import com.capstone.pethouse.infra.mqtt.MqttCommandService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Transactional
@Service
public class FanService {

    private final FanScheduleRepository fanScheduleRepository;
    private final PetHouseRepository petHouseRepository;
    private final MqttCommandService mqttCommandService;
    private final FanLogRepository fanLogRepository;
    private final com.capstone.pethouse.domain.dashboard.repository.DashboardSensorRepository dashboardSensorRepository;

    private final java.util.concurrent.ConcurrentHashMap<Long, java.time.LocalDateTime> fanStartTimes = new java.util.concurrent.ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public Page<FanScheduleResponse> getFanSchedules(Long houseId, Pageable pageable) {
        Page<FanSchedule> fanSchedulePage = fanScheduleRepository.findByPetHouse_HouseId(houseId, pageable);

        return fanSchedulePage.map(FanScheduleResponse::from);
    }

    public FanScheduleResponse postFanSchedule(Long houseId, FanScheduleRequest fanScheduleRequest) {
        if (fanScheduleRequest.endTime().isBefore(fanScheduleRequest.startTime())) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 빨라야 합니다.");
        }

        if (fanScheduleRepository.existingOverlappingSchedule(houseId, fanScheduleRequest.startTime(), fanScheduleRequest.endTime())) {
            throw new IllegalStateException("해당 시간대에 이미 겹치는 팬 스케줄이 존재합니다.");
        }

        PetHouse petHouse = petHouseRepository.getReferenceById(houseId);
        FanSchedule fanSchedule = FanSchedule.of(petHouse, fanScheduleRequest.startTime(), fanScheduleRequest.endTime());

        validateFanSpeedLogic(fanScheduleRequest.fanScheduleDetailRequestList());

        fanScheduleRequest.fanScheduleDetailRequestList()
                .forEach(request -> fanSchedule.addFanScheduleDetail(request.temperature(), request.speed()));

        return FanScheduleResponse.from(fanScheduleRepository.save(fanSchedule));
    }

    public FanScheduleResponse updateFanSchedule(Long houseId, Long scheduleId, FanScheduleRequest fanScheduleRequest) {
        if (fanScheduleRequest.endTime().isBefore(fanScheduleRequest.startTime())) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 빨라야 합니다.");
        }

        if (fanScheduleRepository.existingOverlappingScheduleExcludingSelf(houseId, scheduleId, fanScheduleRequest.startTime(), fanScheduleRequest.endTime())) {
            throw new IllegalStateException("해당 시간대에 이미 겹치는 팬 스케줄이 존재합니다.");        // 자기 자신을 제외한 scheduler 중 확인
        }

        validateFanSpeedLogic(fanScheduleRequest.fanScheduleDetailRequestList());

        FanSchedule fanSchedule = fanScheduleRepository.findByPetHouse_HouseIdAndId(houseId, scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("해당 스케줄을 찾을 수 없습니다."));

        fanSchedule.updateFanSchedule(
                fanScheduleRequest.startTime(),
                fanScheduleRequest.endTime(),
                fanScheduleRequest.fanScheduleDetailRequestList()
        );

        return FanScheduleResponse.from(fanSchedule);
    }

    public FanToggleResponse toggleFanSchedule(Long houseId, Long scheduleId, boolean enabled) {
        FanSchedule fanSchedule = fanScheduleRepository.findByPetHouse_HouseIdAndId(houseId, scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("해당 스케줄을 찾을 수 없습니다."));

        fanSchedule.toggleFanSchedule(enabled);

        return FanToggleResponse.from(fanSchedule);
    }

    public Long deleteFanSchedule(Long houseId, Long scheduleId) {
        FanSchedule fanSchedule = fanScheduleRepository.findByPetHouse_HouseIdAndId(houseId, scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("해당 스케줄을 찾을 수 없습니다."));

        fanScheduleRepository.delete(fanSchedule);

        return fanSchedule.getId();
    }

    public FanControlResponse controlFan(Long houseId, FanControlRequest request) {
        // 1. 펫하우스 존재 여부 확인
        PetHouse petHouse = petHouseRepository.findById(houseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 펫하우스를 찾을 수 없습니다."));

        // 2. MQTT 제어 명령 전송
        Map<String, Object> params = Map.of(
                "isRunning", request.isRunning(),
                "intensity", request.intensity()
        );
        mqttCommandService.sendCommand(houseId, "fanControl", params);

        // 3. 수동 작동 이력(FanLog) 저장 및 관리
        if (request.isRunning()) {
            // 환풍기가 가동을 시작할 때 가동 시작 시각 기록
            fanStartTimes.put(houseId, LocalDateTime.now());
        } else {
            // 환풍기가 정지될 때 가동 이력 기록 생성 및 저장
            LocalDateTime startTime = fanStartTimes.remove(houseId);
            if (startTime == null) {
                // 이전 시작 기록이 없을 경우 최근 10분 동안 작동한 것으로 임시 산정
                startTime = LocalDateTime.now().minusMinutes(10);
            }
            LocalDateTime endTime = LocalDateTime.now();

            // 최신 온도를 InfluxDB에서 획득 시도
            java.math.BigDecimal temperature = java.math.BigDecimal.valueOf(25.0);
            try {
                if (petHouse.getDevices() != null) {
                    String deviceId = petHouse.getDevices().stream()
                            .filter(com.capstone.pethouse.domain.device.entity.Device::isUse)
                            .map(com.capstone.pethouse.domain.device.entity.Device::getDeviceId)
                            .findFirst()
                            .orElse(null);
                    if (deviceId != null) {
                        com.capstone.pethouse.domain.dashboard.dto.response.SensorDataResponse sensorData =
                                dashboardSensorRepository.getLatestSensorData(deviceId);
                        if (sensorData != null && sensorData.temperature() != null) {
                            temperature = java.math.BigDecimal.valueOf(sensorData.temperature());
                        }
                    }
                }
            } catch (Exception e) {
                // 예외 발생 시 기본 온도(25.0) 유지
            }

            Integer speed = request.intensity() != null ? request.intensity() : 50;

            FanLog fanLog = FanLog.of(
                    null, // scheduleId (수동 제어이므로 null)
                    petHouse,
                    temperature,
                    speed,
                    startTime,
                    endTime,
                    com.capstone.pethouse.domain.enums.TriggerType.MANUAL,
                    com.capstone.pethouse.domain.enums.ExecutionStatus.SUCCESS
            );
            fanLogRepository.save(fanLog);
        }

        return FanControlResponse.of(houseId, request.isRunning(), request.intensity());
    }

    public FanAutoModeResponse toggleFanAutoMode(Long houseId, boolean isAutoMode) {
        PetHouse petHouse = petHouseRepository.findById(houseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 펫하우스를 찾을 수 없습니다."));

        petHouse.toggleFanAutoMode(isAutoMode);

        // 시나리오 1(서버 주도 자동 제어) 채택으로, 기기 자체에는 자동/수동 모드 상태를 전송하지 않습니다.
        // 향후 서버의 백그라운드 스케줄러(또는 센서 데이터 수신 핸들러)가 온도값을 모니터링하며 기기에 fanControl 명령만 하향 전송합니다.

        return FanAutoModeResponse.of(houseId, petHouse.getIsFanAutoMode());
    }

    @Transactional(readOnly = true)
    public Page<FanHistoryResponse> getFanHistory(Long houseId, Pageable pageable) {
        petHouseRepository.findById(houseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 펫하우스를 찾을 수 없습니다."));

        return fanLogRepository.findByPetHouse_HouseId(houseId, pageable)
                .map(FanHistoryResponse::from);
    }

    @Transactional(readOnly = true)
    public FanStatsResponse getFanStatistics(Long houseId) {
        petHouseRepository.findById(houseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 펫하우스를 찾을 수 없습니다."));

        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        List<FanLog> logs = 
                fanLogRepository.findByPetHouse_HouseIdAndCreatedAtBetween(houseId, startOfDay, endOfDay);

        if (logs.isEmpty()) {
            return FanStatsResponse.of(0L, 0.0, 0, 0);
        }

        long dailyCount = logs.size();
        long totalMinutes = 0;
        long totalSpeed = 0;
        long autoCount = 0;

        for (com.capstone.pethouse.domain.fan.entity.FanLog log : logs) {
            if (log.getStartTime() != null && log.getEndTime() != null) {
                totalMinutes += Duration.between(log.getStartTime(), log.getEndTime()).toMinutes();
            }
            totalSpeed += log.getSpeed();
            if (com.capstone.pethouse.domain.enums.TriggerType.AUTO.equals(log.getTriggerType())) {
                autoCount++;
            }
        }

        double operatingHours = Math.round((totalMinutes / 60.0) * 10.0) / 10.0;
        int averageIntensity = (int) (totalSpeed / dailyCount);
        int autoModeRatio = (int) Math.round((double) autoCount * 100 / dailyCount);

        return FanStatsResponse.of(dailyCount, operatingHours, averageIntensity, autoModeRatio);
    }

    private void validateFanSpeedLogic(List<FanScheduleDetailRequest> detailRequestList) {
        List<FanScheduleDetailRequest> sortedDetails = detailRequestList.stream()
                .sorted(Comparator.comparing(FanScheduleDetailRequest::temperature))
                .toList();

        for (int i = 0; i < sortedDetails.size() - 1; i++) {
            FanScheduleDetailRequest current = sortedDetails.get(i);
            FanScheduleDetailRequest next = sortedDetails.get(i+1);

            if (next.speed() <= current.speed()) {
                throw new IllegalArgumentException(
                        String.format("온도가 더 높은 설정(%.1f도)의 팬 강도는 이전 설정(%.1f도)보다 높아야 합니다.",
                                next.temperature(), current.temperature())
                );
            }
        }
    }
}
