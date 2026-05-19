package com.capstone.pethouse.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UnitType {
    G("g"),
    ML("ml");

    private final String value;

    UnitType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}
