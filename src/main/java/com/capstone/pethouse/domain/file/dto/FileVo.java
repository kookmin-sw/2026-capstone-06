package com.capstone.pethouse.domain.file.dto;

import com.capstone.pethouse.domain.file.entity.FileInfo;

import java.time.format.DateTimeFormatter;

public record FileVo(
        Long seq,
        String deviceId,
        String filename,
        String regDate
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static FileVo from(FileInfo f) {
        return new FileVo(
                f.getSeq(),
                f.getDeviceId(),
                f.getFilename(),
                f.getRegDate().format(FORMATTER)
        );
    }
}
