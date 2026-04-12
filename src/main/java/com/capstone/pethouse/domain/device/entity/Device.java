package com.capstone.pethouse.domain.device.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
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

    private String objectCode;

    private String objectName;

    @Column(nullable = false)
    private String deviceType;

    private String deviceTypeName;

    private LocalDate objectBirth;

    @Column(nullable = false)
    private boolean isUse;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime regDate;

    private Device(String deviceId, String memberId, String serialNum,
                   String objectCode, String objectName, String deviceType,
                   String deviceTypeName, LocalDate objectBirth, boolean isUse) {
        this.deviceId = deviceId;
        this.memberId = memberId;
        this.serialNum = serialNum;
        this.objectCode = objectCode;
        this.objectName = objectName;
        this.deviceType = deviceType;
        this.deviceTypeName = deviceTypeName;
        this.objectBirth = objectBirth;
        this.isUse = isUse;
    }

    public static Device of(String deviceId, String memberId, String serialNum,
                             String objectCode, LocalDate objectBirth, String deviceType) {
        return new Device(deviceId, memberId, serialNum, objectCode, null, deviceType, null, objectBirth, true);
    }

    public void update(String deviceId, String memberId, String serialNum,
                       String objectCode, LocalDate objectBirth, String deviceType) {
        if (deviceId != null) this.deviceId = deviceId;
        if (memberId != null) this.memberId = memberId;
        if (serialNum != null) this.serialNum = serialNum;
        if (objectCode != null) this.objectCode = objectCode;
        if (objectBirth != null) this.objectBirth = objectBirth;
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
