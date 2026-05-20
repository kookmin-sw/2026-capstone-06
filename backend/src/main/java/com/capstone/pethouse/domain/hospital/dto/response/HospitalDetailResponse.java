package com.capstone.pethouse.domain.hospital.dto.response;

import com.capstone.pethouse.domain.hospital.entity.Hospital;
import java.util.List;

public record HospitalDetailResponse(
        List<MedCodeDto> medList,
        HospitalListResponse hospital
) {
    public static HospitalDetailResponse of(Hospital entity) {
        List<MedCodeDto> mappedMedList = entity.getMedCodes().stream()
                .map(code -> MedCodeDto.of(entity.getSeq(), code))
                .toList();

        return new HospitalDetailResponse(
                mappedMedList,
                HospitalListResponse.from(entity)
        );
    }
}
