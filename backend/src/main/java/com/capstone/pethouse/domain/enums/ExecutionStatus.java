package com.capstone.pethouse.domain.enums;

import lombok.Getter;

@Getter
public enum ExecutionStatus {
    SUCCESS("성공"),
    PROCEEDING("진행중"),
    FAIL("실패");

    private final String description;

    ExecutionStatus(String description) {
        this.description = description;
    }
}
