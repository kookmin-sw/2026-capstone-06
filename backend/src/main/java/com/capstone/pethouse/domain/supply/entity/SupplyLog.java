package com.capstone.pethouse.domain.supply.entity;

import com.capstone.pethouse.domain.device.entity.PetHouse;
import com.capstone.pethouse.domain.enums.ExecutionStatus;
import com.capstone.pethouse.domain.enums.FeedType;
import com.capstone.pethouse.domain.enums.TriggerType;
import com.capstone.pethouse.domain.enums.UnitType;
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
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class SupplyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private SupplySchedule supplySchedule;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "house_id")    // FK 등록, 수정, 삭제
    private PetHouse petHouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedType feedType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitType unitType;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal amount;

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

    private SupplyLog(SupplySchedule supplySchedule, PetHouse petHouse, FeedType feedType, UnitType unitType, BigDecimal amount, ExecutionStatus executionStatus, TriggerType triggerType) {
        this.supplySchedule = supplySchedule;
        this.petHouse = petHouse;
        this.feedType = feedType;
        this.unitType = unitType;
        this.amount = amount;
        this.executionStatus = executionStatus;
        this.triggerType = triggerType;
    }

    public static SupplyLog ofManual(PetHouse petHouse, FeedType feedType, UnitType unitType, BigDecimal amount) {
        return new SupplyLog(
                null,                   // 수동 공급은 schedule이 없음
                petHouse,
                feedType,
                unitType,
                amount,
                ExecutionStatus.PROCEEDING,
                TriggerType.MANUAL
        );
    }

    public static SupplyLog ofScheduled(SupplySchedule supplySchedule, PetHouse petHouse, FeedType feedType, UnitType unitType, BigDecimal amount) {
        return new SupplyLog(
                supplySchedule,
                petHouse,
                feedType,
                unitType,
                amount,
                ExecutionStatus.PROCEEDING,
                TriggerType.AUTO
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplyLog that)) return false;
        return this.id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
