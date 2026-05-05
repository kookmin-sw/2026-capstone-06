package com.capstone.pethouse.domain.device.entity;

import com.capstone.pethouse.domain.User.entity.User;
import com.capstone.pethouse.domain.code.entity.Code;
import com.capstone.pethouse.domain.enums.PetHouseStatus;
import com.capstone.pethouse.global.common.AuditingFields;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ToString(callSuper = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "pet_house",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pet_house_user_id_nickname",
                        columnNames = {"user_id", "nickname"}
                )
        }
)
@Entity
public class PetHouse extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long houseId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetHouseStatus petHouseStatus;

    @Column(nullable = false)
    private Boolean isOccupied;

    @Column(nullable = false)
    private LocalDateTime lastConnectedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "object_code_seq")
    private Code objectCode;

    private String objectName;

    private LocalDate objectBirth;

    @ToString.Exclude
    @OneToMany(mappedBy = "petHouse", cascade = CascadeType.ALL)
    private List<Device> devices = new ArrayList<>();

    private PetHouse(User user, String nickname, PetHouseStatus petHouseStatus, Boolean isOccupied, LocalDateTime lastConnectedAt, Code objectCode, String objectName, java.time.LocalDate objectBirth) {
        this.user = user;
        this.nickname = nickname;
        this.petHouseStatus = petHouseStatus;
        this.isOccupied = isOccupied;
        this.lastConnectedAt = lastConnectedAt;
        this.objectCode = objectCode;
        this.objectName = objectName;
        this.objectBirth = objectBirth;
    }

    public static PetHouse of(User user, String nickname, PetHouseStatus petHouseStatus, Boolean isOccupied, LocalDateTime lastConnectedAt, Code objectCode, String objectName, java.time.LocalDate objectBirth) {
        return new PetHouse(user, nickname, petHouseStatus, isOccupied, lastConnectedAt, objectCode, objectName, objectBirth);
    }

    public static PetHouse createDefault(User user, String nickname, Code objectCode, String objectName, LocalDate objectBirth) {
        return new PetHouse(
                user,
                nickname,
                PetHouseStatus.OFFLINE,
                false,
                LocalDateTime.now(),
                objectCode,
                objectName,
                objectBirth
        );
    }

    public void updatePetInfo(Code objectCode, String objectName, LocalDate objectBirth) {
        if (objectCode != null) this.objectCode = objectCode;
        if (objectName != null) this.objectName = objectName;
        if (objectBirth != null) this.objectBirth = objectBirth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PetHouse that)) return false;
        return this.houseId != null && Objects.equals(this.houseId, that.houseId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(houseId);
    }
}
