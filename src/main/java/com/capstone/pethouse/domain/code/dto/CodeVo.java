package com.capstone.pethouse.domain.code.dto;

import com.capstone.pethouse.domain.code.entity.Code;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public record CodeVo(
        String code,
        String groupCode,
        String codeName,
        String regDate,
        List<CodeVo> children
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static CodeVo from(Code code) {
        return new CodeVo(
                code.getCode(),
                code.getGroupCode(),
                code.getCodeName(),
                code.getRegDate().format(FORMATTER),
                List.of()
        );
    }

    public static CodeVo withChildren(Code code, List<CodeVo> children) {
        return new CodeVo(
                code.getCode(),
                code.getGroupCode(),
                code.getCodeName(),
                code.getRegDate().format(FORMATTER),
                children
        );
    }
}
