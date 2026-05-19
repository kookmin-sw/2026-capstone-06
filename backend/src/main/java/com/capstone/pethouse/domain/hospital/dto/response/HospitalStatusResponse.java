package com.capstone.pethouse.domain.hospital.dto.response;

public record HospitalStatusResponse(
        String status,
        Long hospitalSeq
) {
    public static HospitalStatusResponse success(Long hospitalSeq) {
        return new HospitalStatusResponse("success", hospitalSeq);
    }
    
    public static HospitalStatusResponse success() {
        return new HospitalStatusResponse("success", null);
    }
}
