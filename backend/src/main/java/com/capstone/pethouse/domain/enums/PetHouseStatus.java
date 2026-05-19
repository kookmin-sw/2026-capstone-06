package com.capstone.pethouse.domain.enums;

import lombok.Getter;

@Getter
public enum PetHouseStatus {
    ONLINE("온라인"),
    OFFLINE("오프라인"),
    SLEEP("절전모드");

    private final String description;

    PetHouseStatus(String description) {
        this.description = description;
    }
}
