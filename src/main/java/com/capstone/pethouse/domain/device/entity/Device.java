package com.capstone.pethouse.domain.device.entity;

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
@Table(name = "device")
@Entity
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(nullable = false, unique = true)
    private String deviceId;

    @Column(nullable = false)
    private String memberId;

    @Column(nullable = false)
    private String serialNum;

    @Column(nullable = false)
    private String deviceType;

    @Column(nullable = false)
    private boolean isUse;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime regDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id")
    private PetHouse petHouse;

    private Device(String deviceId, String memberId, String serialNum,
                   String deviceType, boolean isUse, PetHouse petHouse) {
        this.deviceId = deviceId;
        this.memberId = memberId;
        this.serialNum = serialNum;
        this.deviceType = deviceType;
        this.isUse = isUse;
        this.petHouse = petHouse;
    }

    public static Device of(String deviceId, String memberId, String serialNum, String deviceType) {
        return new Device(deviceId, memberId, serialNum, deviceType, true, null);
    }

    public void assignToPetHouse(PetHouse petHouse) {
        this.petHouse = petHouse;
    }

    public void update(String deviceId, String memberId, String serialNum, String deviceType) {
        if (deviceId != null) this.deviceId = deviceId;
        if (memberId != null) this.memberId = memberId;
        if (serialNum != null) this.serialNum = serialNum;
        if (deviceType != null) this.deviceType = deviceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Device that)) return false;
        return this.seq != null && Objects.equals(this.seq, that.seq);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(seq);
    }
}
