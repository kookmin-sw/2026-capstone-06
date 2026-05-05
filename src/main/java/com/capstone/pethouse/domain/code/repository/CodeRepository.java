package com.capstone.pethouse.domain.code.repository;

import com.capstone.pethouse.domain.code.entity.Code;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeRepository extends JpaRepository<Code, Long> {

    Optional<Code> findByCode(String code);

    List<Code> findByParent(Code parent);

    Page<Code> findByParent(Code parent, Pageable pageable);

    @Query("select c from Code c where c.parent.code = :groupCode or c.code = :code")
    List<Code> findByGroupCodeOrCode(@Param("groupCode") String groupCode, @Param("code") String code);
}
