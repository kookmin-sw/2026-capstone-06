package com.capstone.pethouse.domain.User.repository;

import com.capstone.pethouse.domain.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByMemberId(String memberId);

    boolean existsByMemberId(String memberId);

    Optional<User> findByMemberNameAndMemberPhone(String memberName, String memberPhone);

    Optional<User> findByMemberIdAndMemberNameAndMemberPhone(String memberId, String memberName, String memberPhone);
}
