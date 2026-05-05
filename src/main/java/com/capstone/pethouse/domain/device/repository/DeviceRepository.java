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

    Optional<Device> findByDeviceId(String deviceId);

    @Query("SELECT d FROM Device d JOIN FETCH d.user LEFT JOIN FETCH d.petHouse LEFT JOIN FETCH d.petHouse.objectCode WHERE " +
            "(:searchQuery IS NULL OR " +
            " (:searchType = 'deviceId' AND d.deviceId LIKE %:searchQuery%) OR " +
            " (:searchType = 'memberId' AND d.user.memberId LIKE %:searchQuery%) OR " +
            " (:searchType = 'serialNum' AND d.serialNum LIKE %:searchQuery%) OR " +
            " (:searchType IS NULL AND (d.deviceId LIKE %:searchQuery% OR d.user.memberId LIKE %:searchQuery% OR d.serialNum LIKE %:searchQuery%)))")
    Page<Device> findAllWithSearch(@Param("searchType") String searchType,
                                   @Param("searchQuery") String searchQuery,
                                   Pageable pageable);

    @Query("SELECT d FROM Device d JOIN FETCH d.user")
    List<Device> findAllPopupList();

    @Query("SELECT d FROM Device d JOIN FETCH d.user LEFT JOIN FETCH d.petHouse LEFT JOIN FETCH d.petHouse.objectCode WHERE d.deviceType = :deviceType")
    List<Device> findByDeviceType(@Param("deviceType") String deviceType);

    @Query("SELECT d FROM Device d JOIN FETCH d.user LEFT JOIN FETCH d.petHouse LEFT JOIN FETCH d.petHouse.objectCode WHERE d.user.memberId = :memberId")
    List<Device> findByMemberId(@Param("memberId") String memberId);
}
