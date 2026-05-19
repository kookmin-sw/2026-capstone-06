package com.capstone.pethouse.domain.enums;

import lombok.Getter;

@Getter
public enum TriggerType {
    MANUAL("수동"),
    AUTO("자동");

    private final String description;

    TriggerType(String description) {
        this.description = description;
    }
}
