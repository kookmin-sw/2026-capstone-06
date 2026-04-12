package com.capstone.pethouse.domain.serial.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SerialRequest(
        Long seq,
        String serialNum,
        @JsonProperty("isUse") Boolean isUse
) {
}
