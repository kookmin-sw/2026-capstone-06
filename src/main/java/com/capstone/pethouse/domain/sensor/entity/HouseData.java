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
@Table(name = "house_data", indexes = {
        @Index(name = "idx_house_data_device_id", columnList = "deviceId"),
        @Index(name = "idx_house_data_reg_date", columnList = "regDate")
})
@Entity
public class HouseData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(nullable = false)
    private String deviceId;

    private Double temVal;

    private Double humVal;

    private Double coVal;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime regDate;

    private HouseData(String deviceId, Double temVal, Double humVal, Double coVal) {
        this.deviceId = deviceId;
        this.temVal = temVal;
        this.humVal = humVal;
        this.coVal = coVal;
    }

    public static HouseData of(String deviceId, Double temVal, Double humVal, Double coVal) {
        return new HouseData(deviceId, temVal, humVal, coVal);
    }

    public void update(String deviceId, Double temVal, Double humVal, Double coVal) {
        if (deviceId != null) this.deviceId = deviceId;
        if (temVal != null) this.temVal = temVal;
        if (humVal != null) this.humVal = humVal;
        if (coVal != null) this.coVal = coVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HouseData that)) return false;
        return this.seq != null && Objects.equals(this.seq, that.seq);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(seq);
    }
}
