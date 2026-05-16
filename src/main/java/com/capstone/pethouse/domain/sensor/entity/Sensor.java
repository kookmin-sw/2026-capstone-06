package com.capstone.pethouse.domain.sensor.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "sensor_data", indexes = {
        @Index(name = "idx_sensor_data_device_id", columnList = "deviceId"),
        @Index(name = "idx_sensor_data_reg_date", columnList = "regDate")
})
@Entity
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(nullable = false)
    private String deviceId;

    private Double t;

    private Double h;

    private Double co;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime regDate;

    private Sensor(String deviceId, Double t, Double h, Double co) {
        this.deviceId = deviceId;
        this.t = t;
        this.h = h;
        this.co = co;
    }

    public static Sensor of(String deviceId, Double t, Double h, Double co) {
        return new Sensor(deviceId, t, h, co);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sensor that)) return false;
        return this.seq != null && Objects.equals(this.seq, that.seq);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(seq);
    }
}
