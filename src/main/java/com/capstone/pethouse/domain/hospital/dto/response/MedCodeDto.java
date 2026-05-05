package com.capstone.pethouse.domain.hospital.dto.response;

import com.capstone.pethouse.domain.code.entity.Code;

public record MedCodeDto(
        Long hospitalSeq,
        String medCode
) {
    public static MedCodeDto of(Long hospitalSeq, Code code) {
        return new MedCodeDto(hospitalSeq, code != null ? code.getCode() : null);
    }
}
