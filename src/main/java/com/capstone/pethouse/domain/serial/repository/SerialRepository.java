package com.capstone.pethouse.domain.serial.repository;

import com.capstone.pethouse.domain.serial.entity.Serial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SerialRepository extends JpaRepository<Serial, Long> {

    Optional<Serial> findBySerialNum(String serialNum);

    boolean existsBySerialNum(String serialNum);

    @Query("SELECT s FROM Serial s WHERE " +
            "(:searchQuery IS NULL OR s.serialNum LIKE %:searchQuery%)")
    Page<Serial> findAllWithSearch(@Param("searchQuery") String searchQuery, Pageable pageable);
}
