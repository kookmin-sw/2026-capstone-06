package com.capstone.pethouse.domain.hospital.service;

import com.capstone.pethouse.domain.code.entity.Code;
import com.capstone.pethouse.domain.code.repository.CodeRepository;
import com.capstone.pethouse.domain.hospital.dto.request.HospitalRequest;
import com.capstone.pethouse.domain.hospital.dto.response.HospitalDetailResponse;
import com.capstone.pethouse.domain.hospital.dto.response.HospitalListResponse;
import com.capstone.pethouse.domain.hospital.dto.response.HospitalStatusResponse;
import com.capstone.pethouse.domain.hospital.entity.Hospital;
import com.capstone.pethouse.domain.hospital.repository.HospitalRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalService {

        private final HospitalRepository hospitalRepository;
        private final CodeRepository codeRepository;

        public Page<HospitalListResponse> getHospitalList(String searchType, String searchQuery, Pageable pageable) {
                String cleanedQuery = (searchQuery != null && !searchQuery.isBlank()) ? searchQuery.trim() : null;

                Page<Hospital> hospitals = hospitalRepository.searchHospitals(searchType, cleanedQuery, pageable);
                return hospitals.map(HospitalListResponse::from);
        }

        public HospitalDetailResponse getHospital(Long seq) {
                Hospital hospital = hospitalRepository.findById(seq)
                                .orElseThrow(() -> new EntityNotFoundException("Hospital not found: " + seq));
                return HospitalDetailResponse.of(hospital);
        }

        @Transactional
        public HospitalStatusResponse createHospital(HospitalRequest request) {
                Code mainMedCode = codeRepository.findByCode(request.mainMedCode())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Main medical code not found: " + request.mainMedCode()));

                List<Code> medCodes = (request.medCodes() == null || request.medCodes().isEmpty())
                                ? Collections.emptyList()
                                : request.medCodes().stream()
                                                .map(codeStr -> codeRepository.findByCode(codeStr)
                                                                .orElseThrow(() -> new EntityNotFoundException(
                                                                                "Medical code not found: " + codeStr)))
                                                .collect(Collectors.toList());

                Hospital hospital = Hospital.of(
                                request.name(),
                                request.location(),
                                request.phone(),
                                request.latitude(),
                                request.longitude(),
                                mainMedCode,
                                medCodes);
                Hospital saved = hospitalRepository.save(hospital);
                return HospitalStatusResponse.success(saved.getSeq());
        }

        @Transactional
        public HospitalStatusResponse updateHospital(Long seq, HospitalRequest request) {
                Hospital hospital = hospitalRepository.findById(seq)
                                .orElseThrow(() -> new EntityNotFoundException("Hospital not found: " + seq));

                Code mainMedCode = codeRepository.findByCode(request.mainMedCode())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Main medical code not found: " + request.mainMedCode()));

                List<Code> medCodes = (request.medCodes() == null || request.medCodes().isEmpty())
                                ? Collections.emptyList()
                                : request.medCodes().stream()
                                                .map(codeStr -> codeRepository.findByCode(codeStr)
                                                                .orElseThrow(() -> new EntityNotFoundException(
                                                                                "Medical code not found: " + codeStr)))
                                                .collect(Collectors.toList());

                hospital.update(
                                request.name(),
                                request.location(),
                                request.phone(),
                                request.latitude(),
                                request.longitude(),
                                mainMedCode,
                                medCodes);

                return HospitalStatusResponse.success();
        }

        @Transactional
        public HospitalStatusResponse deleteHospital(Long seq) {
                Hospital hospital = hospitalRepository.findById(seq)
                                .orElseThrow(() -> new EntityNotFoundException("Hospital not found: " + seq));
                hospitalRepository.delete(hospital);
                return HospitalStatusResponse.success();
        }
}
