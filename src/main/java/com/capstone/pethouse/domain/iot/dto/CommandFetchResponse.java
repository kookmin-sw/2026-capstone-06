package com.capstone.pethouse.domain.iot.dto;

import com.capstone.pethouse.domain.iot.entity.DeviceCommand;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 디바이스 명세 v0.3 - 응답: [{ "SEQ": Integer, "CT": String }]
 */
public record CommandFetchResponse(
        @JsonProperty("SEQ") Long seq,
        @JsonProperty("CT") String ct
) {
    public static CommandFetchResponse from(DeviceCommand c) {
        return new CommandFetchResponse(c.getSeq(), c.getCt());
    }
}
