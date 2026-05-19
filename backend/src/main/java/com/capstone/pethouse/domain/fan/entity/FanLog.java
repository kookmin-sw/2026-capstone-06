package com.capstone.pethouse.domain.fan.entity;

import com.capstone.pethouse.domain.device.entity.PetHouse;
import com.capstone.pethouse.domain.enums.ExecutionStatus;
import com.capstone.pethouse.domain.enums.TriggerType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
public class FanLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private FanSchedule fanSchedule;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", nullable = false)
    private PetHouse petHouse;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal temperature;

    @Column(nullable = false)
    private Integer speed;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus executionStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerType triggerType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private FanLog(FanSchedule fanSchedule, PetHouse petHouse, BigDecimal temperature, Integer speed, LocalDateTime startTime, LocalDateTime endTime, TriggerType triggerType, ExecutionStatus executionStatus) {
        this.fanSchedule = fanSchedule;
        this.petHouse = petHouse;
        this.temperature = temperature;
        this.speed = speed;
        this.startTime = startTime;
        this.endTime = endTime;
        this.triggerType = triggerType;
        this.executionStatus = executionStatus;
    }

    public static FanLog of(FanSchedule fanSchedule, PetHouse petHouse, BigDecimal temperature, Integer speed, LocalDateTime startTime, LocalDateTime endTime, TriggerType triggerType, ExecutionStatus executionStatus) {
        return new FanLog(fanSchedule, petHouse, temperature, speed, startTime, endTime, triggerType, executionStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FanLog that)) return false;
        return this.id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
