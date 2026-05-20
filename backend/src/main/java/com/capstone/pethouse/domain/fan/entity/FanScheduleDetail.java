package com.capstone.pethouse.domain.fan.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class FanScheduleDetail {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private FanSchedule fanSchedule;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal temperature;

    @Column(nullable = false)
    private Integer speed;

    private FanScheduleDetail(FanSchedule fanSchedule, BigDecimal temperature, Integer speed) {
        this.fanSchedule = fanSchedule;
        this.temperature = temperature;
        this.speed = speed;
    }

    public static FanScheduleDetail of(FanSchedule fanSchedule, BigDecimal temperature, Integer speed) {
        return new FanScheduleDetail(fanSchedule, temperature, speed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FanScheduleDetail that)) return false;
        return this.id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
