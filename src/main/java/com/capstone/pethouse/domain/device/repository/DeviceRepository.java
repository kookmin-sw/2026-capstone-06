package com.capstone.pethouse.domain.device.repository;

import com.capstone.pethouse.domain.device.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findBySerialNum(String serialNum);

    boolean existsBySerialNum(String serialNum);

    boolean existsByDeviceId(String deviceId);

    @Query("SELECT d FROM Device d WHERE " +
            "(:searchQuery IS NULL OR " +
            " (:searchType = 'deviceId' AND d.deviceId LIKE %:searchQuery%) OR " +
            " (:searchType = 'memberId' AND d.memberId LIKE %:searchQuery%) OR " +
            " (:searchType = 'serialNum' AND d.serialNum LIKE %:searchQuery%) OR " +
            " (:searchType IS NULL AND (d.deviceId LIKE %:searchQuery% OR d.memberId LIKE %:searchQuery% OR d.serialNum LIKE %:searchQuery%)))")
    Page<Device> findAllWithSearch(@Param("searchType") String searchType,
                                   @Param("searchQuery") String searchQuery,
                                   Pageable pageable);

    @Query("SELECT d FROM Device d")
    List<Device> findAllPopupList();

    List<Device> findByDeviceType(String deviceType);
}
