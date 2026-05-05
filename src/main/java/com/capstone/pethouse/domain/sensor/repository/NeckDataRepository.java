package com.capstone.pethouse.domain.sensor.repository;

import com.capstone.pethouse.domain.sensor.entity.NeckData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NeckDataRepository extends JpaRepository<NeckData, Long> {

    @Query("SELECT n FROM NeckData n WHERE " +
            "(:searchQuery IS NULL OR :searchQuery = '' OR n.deviceId LIKE %:searchQuery%) " +
            "ORDER BY n.regDate DESC")
    Page<NeckData> findAllWithSearch(@Param("searchQuery") String searchQuery, Pageable pageable);
}
