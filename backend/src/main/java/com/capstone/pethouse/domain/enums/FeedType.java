package com.capstone.pethouse.domain.enums;

import lombok.Getter;

@Getter
public enum FeedType {
    FOOD("급식"),
    WATER("급수");

    private final String description;

    FeedType(String description) {
        this.description = description;
    }
}
