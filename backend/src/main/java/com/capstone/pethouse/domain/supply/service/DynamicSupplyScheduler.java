package com.capstone.pethouse.domain.supply.service;

import com.capstone.pethouse.domain.supply.entity.SupplySchedule;
import com.capstone.pethouse.domain.supply.repository.SupplyScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class DynamicSupplyScheduler {

    private final TaskScheduler poolScheduler;
    private final SupplyScheduleRepository supplyScheduleRepository;
    private final SupplyExecutorService supplyExecutorService; // 실행 로직을 담은 별도 빈 (Self-invocation 문제 해결)

    // 메모리에서 스케줄을 추적하기 위한 맵: scheduleId -> ScheduledFuture
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * 서버 시작 시, DB에 저장되어 있는 '활성화된' 모든 스케줄을 로드하여 등록합니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initSchedulesOnStartup() {
        log.info("초기화: 활성화된 급식/급수 스케줄 로드를 시작합니다.");
        // JOIN FETCH로 PetHouse까지 한번에 로드 (LazyInitializationException 방지)
        List<SupplySchedule> activeSchedules = supplyScheduleRepository.findAllEnabledWithPetHouse();

        for (SupplySchedule schedule : activeSchedules) {
            startSchedule(schedule);
        }
        log.info("초기화: 총 {}개의 스케줄이 성공적으로 등록되었습니다.", activeSchedules.size());
    }

    /**
     * 새로운 스케줄을 등록합니다. (기존에 있다면 중지 후 재등록)
     */
    public void startSchedule(SupplySchedule schedule) {
        stopSchedule(schedule.getId());

        // ★ 핵심: 엔티티 객체가 아닌 원시 값(ID)만 Runnable에 캡처
        // → 스케줄러 스레드에서 나중에 실행될 때 Hibernate 세션이 없어 Lazy 로딩이 터지는 문제 방지
        final Long scheduleId = schedule.getId();
        final String cronExpression = schedule.getCronExpression();

        Runnable task = () -> {
            try {
                log.info("스케줄 실행 시작: scheduleId={}", scheduleId);
                // 실행 시점에 DB에서 최신 데이터를 다시 조회 (PetHouse JOIN FETCH 포함)
                supplyScheduleRepository.findByIdWithPetHouse(scheduleId)
                        .filter(SupplySchedule::isEnabled)
                        .ifPresentOrElse(
                                supplyExecutorService::executeSupplyCommand,
                                () -> log.warn("스케줄 실행 취소: scheduleId={}가 비활성화되었거나 삭제되었습니다.", scheduleId)
                        );
            } catch (Exception e) {
                log.error("스케줄 실행 중 오류 발생: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
            }
        };

        try {
            // 프론트엔드는 UTC 기준으로 cron을 전송하므로 UTC TimeZone 명시
            CronTrigger cronTrigger = new CronTrigger(cronExpression, TimeZone.getTimeZone("UTC"));
            ScheduledFuture<?> scheduledFuture = poolScheduler.schedule(task, cronTrigger);
            scheduledTasks.put(scheduleId, scheduledFuture);
            log.info("스케줄 등록 완료: scheduleId={}, cron={}", scheduleId, cronExpression);
        } catch (Exception e) {
            log.error("스케줄 등록 실패: scheduleId={}, cron={}, error={}",
                    scheduleId, cronExpression, e.getMessage());
        }
    }

    /**
     * 실행 중인 스케줄을 중지(제거)합니다.
     */
    public void stopSchedule(Long scheduleId) {
        ScheduledFuture<?> scheduledFuture = scheduledTasks.get(scheduleId);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledTasks.remove(scheduleId);
            log.info("스케줄 중지 완료: scheduleId={}", scheduleId);
        }
    }
}
