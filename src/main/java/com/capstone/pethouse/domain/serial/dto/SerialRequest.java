package com.capstone.pethouse.domain.serial.dto;

import jakarta.validation.constraints.NotBlank;

public record SerialRequest(
                Long seq,
                @NotBlank(message = "시리얼 번호는 필수입니다.") String serialNum,
                Boolean isUse) {
}
