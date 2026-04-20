package com.capstone.pethouse.domain.hospital.controller;

import com.capstone.pethouse.domain.hospital.dto.request.HospitalCreateRequest;
import com.capstone.pethouse.domain.hospital.dto.request.HospitalUpdateRequest;
import com.capstone.pethouse.domain.hospital.dto.response.HospitalDetailResponse;
import com.capstone.pethouse.domain.hospital.dto.response.HospitalListResponse;
import com.capstone.pethouse.domain.hospital.dto.response.HospitalStatusResponse;
import com.capstone.pethouse.domain.hospital.service.HospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hospital") // The API path is /api/hospital (the /api prefix is set in application properties)
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping("/list")
    public Page<HospitalListResponse> getHospitalList(
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String searchQuery,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        return hospitalService.getHospitalList(searchType, searchQuery, pageable);
    }

    @GetMapping("/{seq}")
    public HospitalDetailResponse getHospital(@PathVariable Long seq) {
        return hospitalService.getHospital(seq);
    }

    @PostMapping
    public HospitalStatusResponse createHospital(@Valid @RequestBody HospitalCreateRequest request) {
        return hospitalService.createHospital(request);
    }

    @PutMapping("/{seq}")
    public HospitalStatusResponse updateHospital(
            @PathVariable Long seq,
            @Valid @RequestBody HospitalUpdateRequest request
    ) {
        return hospitalService.updateHospital(seq, request);
    }

    @DeleteMapping("/{seq}")
    public HospitalStatusResponse deleteHospital(@PathVariable Long seq) {
        return hospitalService.deleteHospital(seq);
    }
}
