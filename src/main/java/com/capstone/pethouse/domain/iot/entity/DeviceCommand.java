package com.capstone.pethouse.domain.iot.entity;

import com.capstone.pethouse.domain.iot.enums.CommandStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 서버 → 디바이스 명령 큐.
 * 디바이스가 /api/command/fetch로 폴링하여 자기 SN의 W 상태 명령을 가져감.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "device_command", indexes = {
        @Index(name = "idx_device_command_sn_status", columnList = "sn,status"),
        @Index(name = "idx_device_command_status", columnList = "status")
})
@Entity
public class DeviceCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(nullable = false)
    private String sn;

    /**
     * 명령 타입 (예: FEED, WATER, FAN_ON, FAN_OFF)
     */
    @Column(nullable = false)
    private String ct;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private CommandStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private DeviceCommand(String sn, String ct, CommandStatus status) {
        this.sn = sn;
        this.ct = ct;
        this.status = status;
    }

    /**
     * 큐에 새 명령 추가 (W 상태로 시작). B 도메인에서 호출.
     */
    public static DeviceCommand enqueue(String sn, String ct) {
        return new DeviceCommand(sn, ct, CommandStatus.W);
    }

    /** fetch 시 호출 — W → S 전환 */
    public void markSent() {
        this.status = CommandStatus.S;
    }

    /** result 시 호출 — S → E 전환 (FEN처럼 보존이 필요한 경우만) */
    public void markExecuted() {
        this.status = CommandStatus.E;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceCommand that)) return false;
        return this.seq != null && Objects.equals(this.seq, that.seq);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(seq);
    }
}
