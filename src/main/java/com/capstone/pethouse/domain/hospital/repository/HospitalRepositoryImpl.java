package com.capstone.pethouse.domain.hospital.repository;

import com.capstone.pethouse.domain.hospital.entity.Hospital;
import static com.capstone.pethouse.domain.hospital.entity.QHospital.hospital;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HospitalRepositoryImpl implements HospitalRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Hospital> searchHospitals(String searchType, String searchQuery, Pageable pageable) {
        List<Hospital> content = queryFactory
                .selectFrom(hospital)
                .leftJoin(hospital.mainMedCode).fetchJoin()
                .where(searchCondition(searchType, searchQuery))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(hospital.createdAt.desc())
                .fetch();

        long total = queryFactory
                .select(hospital.count())
                .from(hospital)
                .where(searchCondition(searchType, searchQuery))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression searchCondition(String searchType, String searchQuery) {
        if (searchType == null || searchQuery == null || searchQuery.trim().isEmpty()) {
            return null;
        }

        return switch (searchType.toLowerCase()) {
            case "name" -> hospital.name.containsIgnoreCase(searchQuery);
            case "location" -> hospital.location.containsIgnoreCase(searchQuery);
            case "medcode" -> hospital.mainMedCode.code.eq(searchQuery);
            default -> null;
        };
    }
}
