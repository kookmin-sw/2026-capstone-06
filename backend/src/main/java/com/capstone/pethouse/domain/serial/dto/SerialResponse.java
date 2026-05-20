package com.capstone.pethouse.domain.serial.dto;

import com.capstone.pethouse.domain.serial.entity.Serial;

import java.time.format.DateTimeFormatter;

public record SerialResponse(
        Long seq,
        String serialNum,
        boolean isUse,
        String regDate) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static SerialResponse from(Serial serial) {
        return new SerialResponse(
                serial.getSeq(),
                serial.getSerialNum(),
                serial.isUse(),
                serial.getRegDate().format(FORMATTER));
    }
}
