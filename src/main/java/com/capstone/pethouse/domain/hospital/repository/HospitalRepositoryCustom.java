package com.capstone.pethouse.domain.hospital.repository;

import com.capstone.pethouse.domain.hospital.entity.Hospital;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HospitalRepositoryCustom {
    Page<Hospital> searchHospitals(String searchType, String searchQuery, Pageable pageable);
}
