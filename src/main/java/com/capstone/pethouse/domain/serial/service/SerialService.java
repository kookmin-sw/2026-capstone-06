package com.capstone.pethouse.domain.serial.service;

import com.capstone.pethouse.domain.serial.dto.SerialRequest;
import com.capstone.pethouse.domain.serial.dto.SerialResponse;
import com.capstone.pethouse.domain.serial.entity.Serial;
import com.capstone.pethouse.domain.serial.repository.SerialRepository;
import com.capstone.pethouse.domain.device.repository.DeviceRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class SerialService {

    private final SerialRepository serialRepository;
    private final DeviceRepository deviceRepository;

    @Transactional(readOnly = true)
    public Page<SerialResponse> getSerials(String searchQuery, Pageable pageable) {
        String cleanedQuery = (searchQuery != null && !searchQuery.isBlank()) ? searchQuery : null;

        return serialRepository.findAllWithSearch(cleanedQuery, pageable).map(SerialResponse::from);
    }

    @Transactional(readOnly = true)
    public SerialResponse getSerial(Long seq) {
        Serial serial = serialRepository.findById(seq)
                .orElseThrow(() -> new EntityNotFoundException("시리얼을 찾을 수 없습니다."));
        return SerialResponse.from(serial);
    }

    @Transactional
    public String addSerial(SerialRequest request) {
        if (serialRepository.existsBySerialNum(request.serialNum())) {
            throw new IllegalStateException("이미 존재하는 시리얼 번호입니다.");
        }
        boolean isUse = request.isUse() != null ? request.isUse() : false;
        serialRepository.save(Serial.of(request.serialNum(), isUse));
        return "등록 성공";
    }

    @Transactional
    public String updateSerial(SerialRequest request) {
        if (request.seq() == null) {
            throw new IllegalArgumentException("시퀀스 번호는 필수입니다.");
        }
        Serial serial = serialRepository.findById(request.seq())
                .orElseThrow(() -> new EntityNotFoundException("시리얼을 찾을 수 없습니다."));

        if (request.serialNum() != null && !request.serialNum().equals(serial.getSerialNum())) {
            if (serialRepository.existsBySerialNum(request.serialNum())) {
                throw new IllegalStateException("이미 존재하는 시리얼 번호입니다.");
            }
        }
        deviceRepository.findBySerialNum(serial.getSerialNum())
                .ifPresent(device -> device.updateSerial(request.serialNum()));

        serial.update(request.serialNum(), request.isUse());

        return "수정 성공";
    }

    @Transactional
    public String deleteSerial(Long seq) {
        Serial serial = serialRepository.findById(seq)
                .orElseThrow(() -> new EntityNotFoundException("시리얼을 찾을 수 없습니다."));

        serialRepository.delete(serial);

        return "삭제 성공";
    }

    @Transactional
    public String markAsUsed(String serialNum) {
        Serial serial = serialRepository.findBySerialNum(serialNum)
                .orElseThrow(() -> new EntityNotFoundException("시리얼을 찾을 수 없습니다."));
        serial.markUsed();
        return "사용 처리 완료";
    }

    @Transactional
    public String markAsUnused(String serialNum) {
        Serial serial = serialRepository.findBySerialNum(serialNum)
                .orElseThrow(() -> new EntityNotFoundException("시리얼을 찾을 수 없습니다."));
        serial.markUnused();
        return "미사용 처리 완료";
    }

    @Transactional
    public List<SerialResponse> generateSerials(int count) {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<Serial> serialsToSave = new ArrayList<>();
        Set<String> existing = new HashSet<>(serialRepository.findSerialNumsByPrefix(datePrefix));
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            String serialNum = null;
            int attempts = 0;
            while (attempts < 100) {
                String candidate = datePrefix + "-DEV-" + String.format("%03d", random.nextInt(1000));
                if (!existing.contains(candidate)) {
                    serialNum = candidate;
                    existing.add(candidate);
                    break;
                }
                attempts++;
            }

            if (serialNum == null) {
                throw new IllegalStateException("시리얼 번호를 생성할 수 없습니다. (중복 횟수 초과)");
            }

            serialsToSave.add(Serial.of(serialNum, false));
        }

        return serialRepository.saveAll(serialsToSave).stream()
                .map(SerialResponse::from)
                .toList();
    }
}
