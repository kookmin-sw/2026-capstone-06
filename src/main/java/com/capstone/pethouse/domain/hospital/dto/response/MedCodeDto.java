package com.capstone.pethouse.domain.hospital.dto.response;

public record MedCodeDto(
        Long hospitalSeq,
        String medCode
) {
    public static MedCodeDto of(Long hospitalSeq, String medCode) {
        return new MedCodeDto(hospitalSeq, medCode);
    }
}
