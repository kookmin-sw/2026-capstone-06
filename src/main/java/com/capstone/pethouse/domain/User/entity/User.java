package com.capstone.pethouse.domain.User.entity;

import com.capstone.pethouse.domain.device.entity.PetHouse;
import com.capstone.pethouse.domain.enums.RoleType;
import com.capstone.pethouse.global.common.AuditingFields;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@ToString(callSuper = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
@Entity
public class User extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(nullable = false, unique = true)
    private String memberId;

    @Column(nullable = false)
    private String memberPw;

    @Column(nullable = false)
    private String memberName;

    @Column(nullable = false)
    private String memberPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType roleCode;

    @Column(nullable = false)
    private boolean enabled;

    @ToString.Exclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private final Set<PetHouse> petHouseSet = new LinkedHashSet<>();

    private User(String memberId, String memberPw, String memberName, String memberPhone, RoleType roleCode) {
        this.memberId = memberId;
        this.memberPw = memberPw;
        this.memberName = memberName;
        this.memberPhone = memberPhone;
        this.roleCode = roleCode;
        this.enabled = true;
    }

    public static User of(String memberId, String memberPw, String memberName, String memberPhone, RoleType roleCode) {
        return new User(memberId, memberPw, memberName, memberPhone, roleCode);
    }

    public static User ofUser(String memberId, String memberPw, String memberName, String memberPhone) {
        return new User(memberId, memberPw, memberName, memberPhone, RoleType.USER);
    }

    public void update(String memberPw, String memberName, String memberPhone, RoleType roleCode) {
        if (memberPw != null && !memberPw.isBlank()) this.memberPw = memberPw;
        if (memberName != null) this.memberName = memberName;
        if (memberPhone != null) this.memberPhone = memberPhone;
        if (roleCode != null) this.roleCode = roleCode;
    }

    public void updateInfo(String memberName, String memberPhone) {
        if (memberName != null) this.memberName = memberName;
        if (memberPhone != null) this.memberPhone = memberPhone;
    }

    public void updatePassword(String encodedPassword) {
        this.memberPw = encodedPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User that)) return false;
        return this.seq != null && Objects.equals(seq, that.seq);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(seq);
    }
}
