package com.capstone.pethouse.domain.User.repository;

import com.capstone.pethouse.domain.User.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

        Optional<User> findByMemberId(String memberId);

        boolean existsByMemberId(String memberId);

        Optional<User> findByMemberNameAndMemberPhone(String memberName, String memberPhone);

        boolean existsByMemberIdAndMemberNameAndMemberPhone(String memberId, String memberName, String memberPhone);

        Optional<User> findByMemberIdAndMemberNameAndMemberPhone(String memberId, String memberName,
                        String memberPhone);

        @Query("SELECT u FROM User u WHERE " +
                        "(:searchQuery IS NULL OR TRIM(:searchQuery) = '' OR " +
                        " (:searchType = 'memberId' AND LOWER(u.memberId) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) OR "
                        +
                        " (:searchType = 'memberName' AND LOWER(u.memberName) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) OR "
                        +
                        " (:searchType = 'memberPhone' AND u.memberPhone LIKE %:searchQuery%) OR " +
                        " (:searchType = 'roleCode' AND CAST(u.roleCode AS string) LIKE %:searchQuery%) OR " +
                        " (:searchType IS NULL AND (" +
                        "    LOWER(u.memberId) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
                        "    LOWER(u.memberName) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
                        "    u.memberPhone LIKE %:searchQuery% OR " +
                        "    CAST(u.roleCode AS string) LIKE %:searchQuery%)))")
        Page<User> findAllWithSearch(@Param("searchType") String searchType,
                        @Param("searchQuery") String searchQuery,
                        Pageable pageable);
}
