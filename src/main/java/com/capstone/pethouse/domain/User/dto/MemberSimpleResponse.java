package com.capstone.pethouse.domain.User.dto;

import com.capstone.pethouse.domain.User.entity.User;

import java.time.format.DateTimeFormatter;

public record MemberSimpleResponse(
        Long seq,
        String memberId,
        String memberPw,
        String memberName,
        String memberPhone,
        String roleCode,
        String regDate
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static MemberSimpleResponse from(User user) {
        return new MemberSimpleResponse(
                user.getSeq(),
                user.getMemberId(),
                user.getMemberPw(),
                user.getMemberName(),
                user.getMemberPhone(),
                user.getRoleCode().name(),
                user.getCreatedAt().format(FORMATTER)
        );
    }
}
