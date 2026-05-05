package com.capstone.pethouse.domain.iot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 디바이스 명세 v0.3 - POST /api/command/fetch
 * 디바이스가 자기 SN의 대기 명령을 폴링.
 */
public record CommandFetchRequest(
        @JsonProperty("SN") String sn
) {
}
