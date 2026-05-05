package com.capstone.pethouse.domain.iot.service;

import com.capstone.pethouse.domain.iot.dto.CommandFetchResponse;
import com.capstone.pethouse.domain.iot.entity.DeviceCommand;
import com.capstone.pethouse.domain.iot.enums.CommandStatus;
import com.capstone.pethouse.domain.iot.repository.DeviceCommandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 명령 큐 시스템.
 *
 * - enqueue: B 도메인이 사용 (예: Feeder가 "급식 명령 추가")
 * - fetch: 디바이스가 폴링하여 자기 SN의 W 명령 가져감 → S로 전환
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DeviceCommandService {

    private final DeviceCommandRepository deviceCommandRepository;

    /**
     * B 도메인이 명령을 큐에 추가할 때 호출 (인프라 API).
     * 예: FeederService.feedNow() → deviceCommandService.enqueue(sn, "FEED")
     */
    @Transactional
    public DeviceCommand enqueue(String sn, String ct) {
        if (sn == null || sn.isBlank()) {
            throw new IllegalArgumentException("SN은 필수입니다.");
        }
        if (ct == null || ct.isBlank()) {
            throw new IllegalArgumentException("CT(명령 타입)는 필수입니다.");
        }
        return deviceCommandRepository.save(DeviceCommand.enqueue(sn, ct));
    }

    /**
     * 디바이스 명세 v0.3 - POST /api/command/fetch
     *
     * 해당 SN의 W 상태 명령을 모두 반환하고 status를 S로 전환.
     * 같은 명령이 중복 fetch되지 않도록 보장.
     */
    @Transactional
    public List<CommandFetchResponse> fetch(String sn) {
        if (sn == null || sn.isBlank()) {
            throw new IllegalArgumentException("SN은 필수입니다.");
        }

        // PESSIMISTIC_WRITE 락으로 같은 SN 동시 폴링 race 차단
        List<DeviceCommand> waiting = deviceCommandRepository.findForFetchWithLock(sn, CommandStatus.W);

        // W → S 전환 (같은 트랜잭션 내 dirty checking으로 update 됨)
        waiting.forEach(DeviceCommand::markSent);

        log.debug("Command fetch — SN={}, count={}", sn, waiting.size());

        return waiting.stream()
                .map(CommandFetchResponse::from)
                .toList();
    }
}
