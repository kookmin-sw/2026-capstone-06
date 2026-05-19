package com.capstone.pethouse.domain.fan.repository;

import com.capstone.pethouse.domain.fan.entity.FanLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FanLogRepository extends JpaRepository<FanLog, Long> {
    
    // 특정 하우스의 로그 목록을 페이징 처리하여 가져오기 (최근 순)
    Page<FanLog> findByPetHouse_HouseId(Long houseId, Pageable pageable);

    // 오늘 하루 동안 특정 하우스에서 생성된 모든 로그 가져오기
    List<FanLog> findByPetHouse_HouseIdAndCreatedAtBetween(Long houseId, LocalDateTime start, LocalDateTime end);
}
