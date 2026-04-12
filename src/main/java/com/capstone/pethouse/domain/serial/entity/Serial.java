package com.capstone.pethouse.domain.serial.entity;

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
@Table(name = "serial")
@Entity
public class Serial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(nullable = false, unique = true)
    private String serialNum;

    @Column(nullable = false)
    private boolean isUse;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime regDate;

    private Serial(String serialNum, boolean isUse) {
        this.serialNum = serialNum;
        this.isUse = isUse;
    }

    public static Serial of(String serialNum, boolean isUse) {
        return new Serial(serialNum, isUse);
    }

    public void update(String serialNum, boolean isUse) {
        if (serialNum != null) this.serialNum = serialNum;
        this.isUse = isUse;
    }

    public void markUsed() {
        this.isUse = true;
    }

    public void markUnused() {
        this.isUse = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Serial that)) return false;
        return this.seq != null && Objects.equals(this.seq, that.seq);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(seq);
    }
}
