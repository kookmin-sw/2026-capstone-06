package com.capstone.pethouse.domain.supply.entity;

import com.capstone.pethouse.domain.device.entity.PetHouse;
import com.capstone.pethouse.domain.enums.FeedType;
import com.capstone.pethouse.domain.enums.UnitType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "supply_schedule",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_supply_schedule_house_condition",
                        columnNames = {"house_id", "feed_type", "cron_expression"}
            )
        }
)
@Entity
public class SupplySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", nullable = false)
    private PetHouse petHouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedType feedType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitType unitType;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 100)
    private String cronExpression;

    @Column(nullable = false)
    private boolean enabled = true;            // 기본값 true

    @Column
    private LocalDateTime lastRunAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime modifiedAt;

    private SupplySchedule(PetHouse petHouse, FeedType feedType, UnitType unitType, BigDecimal amount, String cronExpression, boolean enabled, LocalDateTime lastRunAt) {
        this.petHouse = petHouse;
        this.feedType = feedType;
        this.unitType = unitType;
        this.amount = amount;
        this.cronExpression = cronExpression;
        this.enabled = enabled;
        this.lastRunAt = lastRunAt;
    }

    public static SupplySchedule of(PetHouse petHouse, FeedType feedType, UnitType unitType, BigDecimal amount, String cronExpression) {
        return new SupplySchedule(petHouse, feedType, unitType, amount, cronExpression, true, null);
    }

    public void updateSupplySchedule(FeedType feedType, UnitType unitType, BigDecimal amount, String cronExpression) {
        this.feedType = feedType;
        this.unitType = unitType;
        this.amount = amount;
        this.cronExpression = cronExpression;
    }

    public void toggleSupplySchedule(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplySchedule that)) return false;
        return this.id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
