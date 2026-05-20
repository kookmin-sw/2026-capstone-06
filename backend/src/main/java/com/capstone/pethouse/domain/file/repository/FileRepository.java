package com.capstone.pethouse.domain.file.repository;

import com.capstone.pethouse.domain.file.entity.FileInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileInfo, Long> {

    Optional<FileInfo> findBySeqAndDeviceId(Long seq, String deviceId);

    @Query("SELECT f FROM FileInfo f WHERE " +
            "(:deviceId IS NULL OR :deviceId = '' OR f.deviceId = :deviceId) AND " +
            "(:searchQuery IS NULL OR :searchQuery = '' OR f.filename LIKE %:searchQuery% OR f.deviceId LIKE %:searchQuery%) " +
            "ORDER BY f.regDate DESC")
    Page<FileInfo> findAllWithSearch(@Param("deviceId") String deviceId,
                                      @Param("searchQuery") String searchQuery,
                                      Pageable pageable);
}
