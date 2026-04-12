package com.capstone.pethouse.domain.code.repository;

import com.capstone.pethouse.domain.code.entity.Code;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeRepository extends JpaRepository<Code, String> {

    List<Code> findByGroupCode(String groupCode);

    Page<Code> findByGroupCode(String groupCode, Pageable pageable);

    List<Code> findByGroupCodeOrCode(String groupCode, String code);
}
