package com.capstone.pethouse.domain.fan.entity;

import com.capstone.pethouse.domain.device.entity.PetHouse;
import com.capstone.pethouse.domain.fan.dto.request.FanScheduleDetailRequest;
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
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@ToString
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "fan_schedule",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fan_schedule_house_condition",
                        columnNames = {"house_id", "start_time", "end_time"}
                )
        }
)
@Entity
public class FanSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", nullable = false)
    private PetHouse petHouse;

    @ToString.Exclude
    @OneToMany(mappedBy = "fanSchedule", cascade = CascadeType.ALL, orphanRemoval=true)
    private final Set<FanScheduleDetail> fanScheduleDetailSet = new LinkedHashSet<>();

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime modifiedAt;

    public void addFanScheduleDetail(BigDecimal temperature, Integer speed) {
        FanScheduleDetail fanScheduleDetail = FanScheduleDetail.of(this, temperature, speed);
        this.fanScheduleDetailSet.add(fanScheduleDetail);
    }

    private FanSchedule(PetHouse petHouse, boolean enabled, LocalTime startTime, LocalTime endTime) {
        this.petHouse = petHouse;
        this.enabled = enabled;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static FanSchedule of(PetHouse petHouse, LocalTime startTime, LocalTime endTime) {
        return new FanSchedule(petHouse, true, startTime, endTime);
    }

    public void updateFanSchedule(LocalTime startTime, LocalTime endTime, List<FanScheduleDetailRequest> fanScheduleDetailRequestList) {
        this.startTime = startTime;
        this.endTime = endTime;

        this.fanScheduleDetailSet.clear();

        fanScheduleDetailRequestList.forEach(detailRequest ->
                this.addFanScheduleDetail(detailRequest.temperature(), detailRequest.speed()));
    }

    public void toggleFanSchedule(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FanSchedule that)) return false;
        return this.id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
