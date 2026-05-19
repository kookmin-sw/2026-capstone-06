package com.capstone.pethouse.domain.code.dto;

import com.capstone.pethouse.domain.code.entity.Code;

import java.time.format.DateTimeFormatter;
import java.util.List;

public record CodeResponse(
        Long seq,
        String code,
        String groupCode,
        String codeName,
        String regDate,
        List<CodeResponse> children) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static CodeResponse from(Code code) {
        return new CodeResponse(
                code.getSeq(),
                code.getCode(),
                code.getParent() != null ? code.getParent().getCode() : null,
                code.getCodeName(),
                code.getRegDate().format(FORMATTER),
                List.of());
    }

    public static CodeResponse withChildren(Code code, List<CodeResponse> children) {
        return new CodeResponse(
                code.getSeq(),
                code.getCode(),
                code.getParent() != null ? code.getParent().getCode() : null,
                code.getCodeName(),
                code.getRegDate().format(FORMATTER),
                children);
    }
}
