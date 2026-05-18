package com.capstone.pethouse.domain.User.dto.response;

import com.capstone.pethouse.domain.User.entity.User;

import java.time.format.DateTimeFormatter;

public record MemberSimpleResponse(
        Long seq,
        String memberId,
        String memberName,
        String memberPhone,
        String roleCode,
        String roleName,
        String regDate) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static MemberSimpleResponse from(User user) {
        String roleName = switch (user.getRoleCode()) {
            case USER -> "일반회원";
            case ADMIN -> "관리자";
        };
        return new MemberSimpleResponse(
                user.getSeq(),
                user.getMemberId(),
                user.getMemberName(),
                user.getMemberPhone(),
                user.getRoleCode().name(),
                roleName,
                user.getCreatedAt().format(FORMATTER));
    }
}
