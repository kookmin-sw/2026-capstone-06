package com.capstone.pethouse.domain.User.dto;

import com.capstone.pethouse.domain.User.entity.User;

import java.time.format.DateTimeFormatter;

public record MemberResponse(
        Long seq,
        String memberId,
        String memberPw,
        String memberName,
        String memberPhone,
        String roleCode,
        String roleName,
        String regDate,
        boolean enabled
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static MemberResponse from(User user) {
        String roleName = switch (user.getRoleCode()) {
            case USER -> "일반회원";
            case ADMIN -> "관리자";
        };
        return new MemberResponse(
                user.getSeq(),
                user.getMemberId(),
                user.getMemberPw(),
                user.getMemberName(),
                user.getMemberPhone(),
                user.getRoleCode().name(),
                roleName,
                user.getCreatedAt().format(FORMATTER),
                user.isEnabled()
        );
    }
}
