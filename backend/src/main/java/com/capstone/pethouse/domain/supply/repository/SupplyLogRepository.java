package com.capstone.pethouse.domain.supply.repository;

import com.capstone.pethouse.domain.supply.entity.SupplyLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplyLogRepository extends JpaRepository<SupplyLog, Long> {
    Page<SupplyLog> findByPetHouse_HouseId(Long houseId, Pageable pageable);
}
