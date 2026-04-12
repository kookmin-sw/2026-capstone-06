package com.capstone.pethouse.domain.code.entity;

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
@Table(name = "code")
@Entity
public class Code {

    @Id
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String groupCode;

    @Column(nullable = false)
    private String codeName;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime regDate;

    private Code(String code, String groupCode, String codeName) {
        this.code = code;
        this.groupCode = groupCode;
        this.codeName = codeName;
    }

    public static Code of(String code, String groupCode, String codeName) {
        return new Code(code, groupCode, codeName);
    }

    public void update(String groupCode, String codeName) {
        if (groupCode != null) this.groupCode = groupCode;
        if (codeName != null) this.codeName = codeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Code that)) return false;
        return Objects.equals(this.code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
