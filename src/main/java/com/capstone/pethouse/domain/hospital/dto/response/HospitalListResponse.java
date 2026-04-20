package com.capstone.pethouse.domain.hospital.dto.response;

import com.capstone.pethouse.domain.hospital.entity.Hospital;
import java.time.format.DateTimeFormatter;

public record HospitalListResponse(
        Long seq,
        String name,
        String location,
        String phone,
        Double latitude,
        Double longitude,
        String mainMedCode,
        String regDate
) {
    public static HospitalListResponse from(Hospital hospital) {
        String regDateStr = hospital.getCreatedAt() != null 
            ? hospital.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            : null;
            
        return new HospitalListResponse(
                hospital.getSeq(),
                hospital.getName(),
                hospital.getLocation(),
                hospital.getPhone(),
                hospital.getLatitude(),
                hospital.getLongitude(),
                hospital.getMainMedCode(),
                regDateStr
        );
    }
}
