package com.capstone.pethouse.domain.sensor.repository;

import com.capstone.pethouse.domain.sensor.entity.HouseData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseDataRepository extends JpaRepository<HouseData, Long> {

    @Query("SELECT h FROM HouseData h WHERE " +
            "(:searchQuery IS NULL OR :searchQuery = '' OR h.deviceId LIKE %:searchQuery%) " +
            "ORDER BY h.regDate DESC")
    Page<HouseData> findAllWithSearch(@Param("searchQuery") String searchQuery, Pageable pageable);
}
