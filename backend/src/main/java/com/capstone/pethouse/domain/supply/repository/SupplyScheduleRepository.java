package com.capstone.pethouse.domain.supply.repository;

import com.capstone.pethouse.domain.enums.FeedType;
import com.capstone.pethouse.domain.supply.entity.SupplySchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplyScheduleRepository extends JpaRepository<SupplySchedule, Long> {
    boolean existsByPetHouse_HouseIdAndFeedTypeAndCronExpression(Long houseId, FeedType feedType, String cronExpression);
    boolean existsByPetHouse_HouseIdAndFeedTypeAndCronExpressionAndIdNot(Long houseId, FeedType feedType, String cronExpression, Long scheduleId);
    Optional<SupplySchedule> findByPetHouse_HouseIdAndId(Long houseId, Long scheduleId);
    Page<SupplySchedule> findByPetHouse_HouseId(Long houseId, Pageable pageable);
}
