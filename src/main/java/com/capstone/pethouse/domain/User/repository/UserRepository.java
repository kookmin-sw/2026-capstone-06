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

    Optional<User> findByMemberIdAndMemberNameAndMemberPhone(String memberId, String memberName, String memberPhone);

    @Query("SELECT u FROM User u WHERE " +
            "(:searchQuery IS NULL OR :searchQuery = '' OR " +
            " (:searchType = 'memberId' AND u.memberId LIKE %:searchQuery%) OR " +
            " (:searchType = 'memberName' AND u.memberName LIKE %:searchQuery%) OR " +
            " (:searchType = 'memberPhone' AND u.memberPhone LIKE %:searchQuery%) OR " +
            " (:searchType = 'roleCode' AND CAST(u.roleCode AS string) LIKE %:searchQuery%) OR " +
            " (:searchType IS NULL AND (" +
            "    u.memberId LIKE %:searchQuery% OR " +
            "    u.memberName LIKE %:searchQuery% OR " +
            "    u.memberPhone LIKE %:searchQuery% OR " +
            "    CAST(u.roleCode AS string) LIKE %:searchQuery%)))")
    Page<User> findAllWithSearch(@Param("searchType") String searchType,
                                 @Param("searchQuery") String searchQuery,
                                 Pageable pageable);
}
