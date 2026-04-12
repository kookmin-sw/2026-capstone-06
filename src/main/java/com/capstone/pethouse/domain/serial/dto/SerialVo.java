package com.capstone.pethouse.domain.serial.dto;

import com.capstone.pethouse.domain.serial.entity.Serial;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.format.DateTimeFormatter;

public record SerialVo(
        Long seq,
        String serialNum,
        @JsonProperty("isUse") boolean isUse,
        String regDate
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static SerialVo from(Serial serial) {
        return new SerialVo(
                serial.getSeq(),
                serial.getSerialNum(),
                serial.isUse(),
                serial.getRegDate().format(FORMATTER)
        );
    }
}
