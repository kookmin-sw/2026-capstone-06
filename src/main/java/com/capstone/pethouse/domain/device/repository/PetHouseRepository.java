package com.capstone.pethouse.domain.device.repository;

import com.capstone.pethouse.domain.User.entity.User;
import com.capstone.pethouse.domain.device.entity.PetHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetHouseRepository extends JpaRepository<PetHouse, Long> {
    List<PetHouse> findByUser(User user);
}
