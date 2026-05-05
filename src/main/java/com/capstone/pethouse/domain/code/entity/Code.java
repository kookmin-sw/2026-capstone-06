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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_seq")
    private Code parent;

    @Column(nullable = false)
    private String codeName;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime regDate;

    private Code(String code, Code parent, String codeName) {
        this.code = code;
        this.parent = parent;
        this.codeName = codeName;
    }

    public static Code of(String code, Code parent, String codeName) {
        return new Code(code, parent, codeName);
    }

    public void update(Code parent, String codeName) {
        if (parent != null) this.parent = parent;
        if (codeName != null) this.codeName = codeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Code that)) return false;
        return Objects.equals(this.seq, that.seq);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(seq);
    }
}
