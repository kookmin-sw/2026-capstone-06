package com.capstone.pethouse.domain.hospital.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record HospitalCreateRequest(
        @NotBlank String name,
        @NotBlank String location,
        @NotBlank String phone,
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotBlank String mainMedCode,
        List<String> medCodes
) {
}
