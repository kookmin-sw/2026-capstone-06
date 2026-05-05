package com.capstone.pethouse.domain.iot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 디바이스 명세 v0.3 - POST /api/data
 * 디바이스가 직접 호출하는 환경 데이터 등록 API.
 * 필드명이 짧음 (대역폭 절약).
 */
public record IotDataRequest(
        @JsonProperty("SN") String sn,    // 시리얼 번호
        @JsonProperty("T") Double t,      // 온도
        @JsonProperty("H") Double h,      // 습도
        @JsonProperty("CO") Double co     // CO 농도
) {
}
