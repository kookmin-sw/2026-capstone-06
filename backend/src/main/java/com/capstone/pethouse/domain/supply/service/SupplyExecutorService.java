package com.capstone.pethouse.domain.supply.service;

import com.capstone.pethouse.domain.enums.FeedType;
import com.capstone.pethouse.domain.supply.entity.SupplyLog;
import com.capstone.pethouse.domain.supply.entity.SupplySchedule;
import com.capstone.pethouse.domain.supply.repository.SupplyLogRepository;
import com.capstone.pethouse.infra.mqtt.MqttCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 실제 급식/급수 명령 실행을 담당하는 서비스.
 * DynamicSupplyScheduler와 별도 빈으로 분리하여 @Transactional이 정상 동작하게 함.
 * (같은 클래스 내 self-invocation 시 AOP 프록시가 우회되는 문제 해결)
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SupplyExecutorService {

    private final SupplyLogRepository supplyLogRepository;
    private final MqttCommandService mqttCommandService;

    @Transactional
    public void executeSupplyCommand(SupplySchedule schedule) {
        Long houseId = schedule.getPetHouse().getHouseId();

        // 1. MQTT 커맨드 발행
        String action = schedule.getFeedType() == FeedType.FOOD ? "SUPPLY_FOOD" : "SUPPLY_WATER";
        Map<String, Object> params = Map.of(
                "feedType", schedule.getFeedType().name(),
                "unitType", schedule.getUnitType().getValue(),
                "amount", schedule.getAmount()
        );
        mqttCommandService.sendCommand(houseId, action, params);
        log.info("예약된 Supply command 발행 완료 — houseId={}, action={}, params={}", houseId, action, params);

        // 2. SupplyLog 이력 저장
        SupplyLog supplyLog = SupplyLog.ofScheduled(
                schedule,
                schedule.getPetHouse(),
                schedule.getFeedType(),
                schedule.getUnitType(),
                schedule.getAmount()
        );
        supplyLogRepository.save(supplyLog);
        log.info("SupplyLog 저장 완료 — scheduleId={}, houseId={}", schedule.getId(), houseId);
    }
}
