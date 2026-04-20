package com.capstone.pethouse.domain.hospital.entity;

import com.capstone.pethouse.global.common.AuditingFields;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hospital extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false, length = 20)
    private String mainMedCode;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "hospital_med_code", joinColumns = @JoinColumn(name = "hospital_seq"))
    @Column(name = "med_code")
    private List<String> medCodes = new ArrayList<>();

    @Builder
    private Hospital(String name, String location, String phone, Double latitude, Double longitude, String mainMedCode, List<String> medCodes) {
        this.name = name;
        this.location = location;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mainMedCode = mainMedCode;
        if (medCodes != null) {
            this.medCodes = medCodes;
        }
    }

    public static Hospital of(String name, String location, String phone, Double latitude, Double longitude, String mainMedCode, List<String> medCodes) {
        return Hospital.builder()
                .name(name)
                .location(location)
                .phone(phone)
                .latitude(latitude)
                .longitude(longitude)
                .mainMedCode(mainMedCode)
                .medCodes(medCodes)
                .build();
    }

    public void update(String name, String location, String phone, Double latitude, Double longitude, String mainMedCode, List<String> medCodes) {
        this.name = name;
        this.location = location;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mainMedCode = mainMedCode;
        this.medCodes.clear();
        if (medCodes != null) {
            this.medCodes.addAll(medCodes);
        }
    }
}
