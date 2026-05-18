package com.capstone.pethouse.domain.dashboard.dto.response;
import com.capstone.pethouse.domain.code.entity.Code;

public record CodeResponse(
        String code,
        String codeName,
        String groupCode
) {
        public static CodeResponse from(Code code) {
                return new CodeResponse(
                        code.getCode(),
                        code.getCodeName(), 
                        code.getParent() != null ? code.getParent().getCode() : null
                );
        }
}
