package com.capstone.pethouse.domain.serial.repository;

import com.capstone.pethouse.domain.serial.entity.Serial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface SerialRepository extends JpaRepository<Serial, Long> {

    Optional<Serial> findBySerialNum(String serialNum);

    Optional<Serial> findById(Long seq);

    @Query("SELECT s.serialNum FROM Serial s WHERE s.serialNum LIKE CONCAT(:prefix, '%')")
    List<String> findSerialNumsByPrefix(String prefix);

    boolean existsBySerialNum(String serialNum);

    @Query("SELECT s FROM Serial s WHERE " +
            "(:searchQuery IS NULL OR TRIM(:searchQuery) = '' OR " +
            "LOWER(s.serialNum) LIKE LOWER(CONCAT('%', :searchQuery, '%')))")
    Page<Serial> findAllWithSearch(@Param("searchQuery") String searchQuery, Pageable pageable);
}
