package com.capstone.pethouse.domain.iot.repository;

import com.capstone.pethouse.domain.iot.entity.DeviceCommand;
import com.capstone.pethouse.domain.iot.enums.CommandStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceCommandRepository extends JpaRepository<DeviceCommand, Long> {

    /**
     * SN의 대기(W) 중인 명령 조회 (오래된 순).
     */
    List<DeviceCommand> findBySnAndStatusOrderBySeqAsc(String sn, CommandStatus status);

    /**
     * fetch 전용 — 같은 SN 동시 폴링 시 중복 전달을 막기 위해 X-Lock 획득.
     *
     * 동작:
     *   - 같은 SN의 W 행에 SELECT ... FOR UPDATE 락
     *   - 두 번째 동시 호출은 첫 호출의 트랜잭션 종료까지 BLOCK
     *   - 첫 호출이 W→S 전환 + 커밋하면, 두 번째는 빈 리스트 받음
     *   - 다른 SN은 영향 없음 (행 단위 락)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM DeviceCommand c WHERE c.sn = :sn AND c.status = :status ORDER BY c.seq ASC")
    List<DeviceCommand> findForFetchWithLock(@Param("sn") String sn,
                                              @Param("status") CommandStatus status);
}
