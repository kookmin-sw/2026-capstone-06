package com.capstone.pethouse.domain.supply.service;

import com.capstone.pethouse.domain.device.entity.PetHouse;
import com.capstone.pethouse.domain.device.repository.PetHouseRepository;
import com.capstone.pethouse.domain.enums.FeedType;
import com.capstone.pethouse.domain.supply.dto.request.SupplyLogRequest;
import com.capstone.pethouse.domain.supply.dto.request.SupplyScheduleRequest;
import com.capstone.pethouse.domain.supply.dto.response.SupplyToggleResponse;
import com.capstone.pethouse.domain.supply.dto.response.SupplyLogHistoryResponse;
import com.capstone.pethouse.domain.supply.dto.response.SupplyLogResponse;
import com.capstone.pethouse.domain.supply.dto.response.SupplyScheduleResponse;
import com.capstone.pethouse.domain.supply.entity.SupplyLog;
import com.capstone.pethouse.domain.supply.entity.SupplySchedule;
import com.capstone.pethouse.domain.supply.repository.SupplyLogRepository;
import com.capstone.pethouse.domain.supply.repository.SupplyScheduleRepository;
import com.capstone.pethouse.infra.mqtt.MqttCommandService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class SupplyService {

    private final SupplyLogRepository supplyLogRepository;
    private final PetHouseRepository petHouseRepository;
    private final SupplyScheduleRepository supplyScheduleRepository;
    private final MqttCommandService mqttCommandService;
    private final DynamicSupplyScheduler dynamicSupplyScheduler;

    @Transactional(readOnly = true)
    public Page<SupplyScheduleResponse> getSupplySchedules(Long houseId, Pageable pageable) {
        Page<SupplySchedule> supplySchedulePage = supplyScheduleRepository.findByPetHouse_HouseId(houseId, pageable);

        return supplySchedulePage.map(SupplyScheduleResponse::from);
    }

    public SupplyScheduleResponse postSupplySchedule(Long houseId, SupplyScheduleRequest supplyScheduleRequest) {
        if (supplyScheduleRepository.existsByPetHouse_HouseIdAndFeedTypeAndCronExpression(houseId, supplyScheduleRequest.feedType(), supplyScheduleRequest.cronExpression())) {
            throw new IllegalStateException("이미 동일한 스케줄이 존재합니다.");
        }

        PetHouse petHouse = petHouseRepository.getReferenceById(houseId);
        SupplySchedule supplySchedule = SupplySchedule.of(
                petHouse, 
                supplyScheduleRequest.feedType(),
                supplyScheduleRequest.unitType(),
                supplyScheduleRequest.amount(),
                supplyScheduleRequest.cronExpression()
        );

        SupplySchedule savedSchedule = supplyScheduleRepository.save(supplySchedule);
        
        // 새로 생성된 스케줄을 메모리에 등록하여 알람 세팅
        dynamicSupplyScheduler.startSchedule(savedSchedule);

        return SupplyScheduleResponse.from(savedSchedule);
    }

    public SupplyScheduleResponse updateSupplySchedule(Long houseId, Long scheduleId, SupplyScheduleRequest supplyScheduleRequest) {
        if (supplyScheduleRepository.existsByPetHouse_HouseIdAndFeedTypeAndCronExpressionAndIdNot(houseId, supplyScheduleRequest.feedType(), supplyScheduleRequest.cronExpression(), scheduleId)) {
            throw new IllegalStateException("이미 해당 시간에 동일한 급여 설정이 존재합니다.");
        }

        SupplySchedule supplySchedule = supplyScheduleRepository.findByPetHouse_HouseIdAndId(houseId, scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("해당 스케줄을 찾을 수 없습니다."));

        supplySchedule.updateSupplySchedule(
                supplyScheduleRequest.feedType(),
                supplyScheduleRequest.unitType(),
                supplyScheduleRequest.amount(),
                supplyScheduleRequest.cronExpression()
        );

        // 변경된 스케줄을 메모리에 다시 세팅 (기존 타이머 취소 후 새 타이머 등록)
        dynamicSupplyScheduler.startSchedule(supplySchedule);

        return SupplyScheduleResponse.from(supplySchedule);
    }

    public SupplyToggleResponse toggleSupplySchedule(Long houseId, Long scheduleId, boolean enabled) {
        SupplySchedule supplySchedule = supplyScheduleRepository.findByPetHouse_HouseIdAndId(houseId, scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("해당 스케줄을 찾을 수 없습니다."));

        supplySchedule.toggleSupplySchedule(enabled);

        // 토글 상태에 따라 타이머 시작 또는 중지
        if (enabled) {
            dynamicSupplyScheduler.startSchedule(supplySchedule);
        } else {
            dynamicSupplyScheduler.stopSchedule(supplySchedule.getId());
        }

        return SupplyToggleResponse.from(supplySchedule);
    }

    public Long deleteSupplySchedule(Long houseId, Long scheduleId) {
        SupplySchedule supplySchedule = supplyScheduleRepository.findByPetHouse_HouseIdAndId(houseId, scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("해당 스케줄을 찾을 수 없습니다."));

        supplyScheduleRepository.delete(supplySchedule);

        // DB에서 삭제되었으므로 알람 타이머도 해제
        dynamicSupplyScheduler.stopSchedule(scheduleId);

        return supplySchedule.getId();
    }

    public SupplyLogResponse recordSupplyLog(Long houseId, SupplyLogRequest supplyLogRequest) {
        PetHouse petHouse = petHouseRepository.getReferenceById(houseId);

        // MQTT 커맨드를 디바이스에 발행 (급식 or 급수)
        String action = supplyLogRequest.feedType() == FeedType.FOOD ? "SUPPLY_FOOD" : "SUPPLY_WATER";
        Map<String, Object> params = Map.of(
                "feedType", supplyLogRequest.feedType().name(),
                "unitType", supplyLogRequest.unitType().getValue(),
                "amount", supplyLogRequest.amount()
        );
        mqttCommandService.sendCommand(houseId, action, params);
        log.info("Supply command sent — houseId={}, action={}, params={}", houseId, action, params);

        SupplyLog supplyLog = SupplyLog.ofManual(
                petHouse,
                supplyLogRequest.feedType(),
                supplyLogRequest.unitType(),
                supplyLogRequest.amount()
        );

        return SupplyLogResponse.from(supplyLogRepository.save(supplyLog));
    }

    @Transactional(readOnly = true)
    public Page<SupplyLogHistoryResponse> getSupplyHistory(Long houseId, Pageable pageable) {
        Page<SupplyLog> supplyLogsPage = supplyLogRepository.findByPetHouse_HouseId(houseId, pageable);

        return supplyLogsPage.map(SupplyLogHistoryResponse::from);
    }
}
