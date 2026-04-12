package com.capstone.pethouse.domain.serial.service;

import com.capstone.pethouse.domain.serial.dto.SerialRequest;
import com.capstone.pethouse.domain.serial.dto.SerialVo;
import com.capstone.pethouse.domain.serial.entity.Serial;
import com.capstone.pethouse.domain.serial.repository.SerialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SerialService {

    private final SerialRepository serialRepository;

    @Transactional(readOnly = true)
    public Page<SerialVo> getSerials(int pageNum, int pageSize, String searchQuery) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "regDate"));
        return serialRepository.findAllWithSearch(searchQuery, pageRequest).map(SerialVo::from);
    }

    @Transactional(readOnly = true)
    public SerialVo getSerial(Long seq) {
        Serial serial = serialRepository.findById(seq)
                .orElseThrow(() -> new IllegalArgumentException("시리얼을 찾을 수 없습니다."));
        return SerialVo.from(serial);
    }

    @Transactional
    public String addSerial(SerialRequest request) {
        if (serialRepository.existsBySerialNum(request.serialNum())) {
            throw new IllegalArgumentException("이미 존재하는 시리얼 번호입니다.");
        }
        boolean isUse = request.isUse() != null ? request.isUse() : false;
        serialRepository.save(Serial.of(request.serialNum(), isUse));
        return "등록 성공";
    }

    @Transactional
    public String updateSerial(SerialRequest request) {
        Serial serial = serialRepository.findById(request.seq())
                .orElseThrow(() -> new IllegalArgumentException("시리얼을 찾을 수 없습니다."));

        if (request.serialNum() != null && !request.serialNum().equals(serial.getSerialNum())) {
            if (serialRepository.existsBySerialNum(request.serialNum())) {
                throw new IllegalArgumentException("이미 존재하는 시리얼 번호입니다.");
            }
        }

        boolean isUse = request.isUse() != null ? request.isUse() : serial.isUse();
        serial.update(request.serialNum(), isUse);
        return "수정 성공";
    }

    @Transactional
    public String deleteSerial(Long seq) {
        if (!serialRepository.existsById(seq)) {
            throw new IllegalArgumentException("시리얼을 찾을 수 없습니다.");
        }
        serialRepository.deleteById(seq);
        return "삭제 성공";
    }

    @Transactional
    public String markAsUsed(String serialNum) {
        Serial serial = serialRepository.findBySerialNum(serialNum)
                .orElseThrow(() -> new IllegalArgumentException("시리얼을 찾을 수 없습니다."));
        serial.markUsed();
        return "사용 처리 완료";
    }

    @Transactional
    public String markAsUnused(String serialNum) {
        Serial serial = serialRepository.findBySerialNum(serialNum)
                .orElseThrow(() -> new IllegalArgumentException("시리얼을 찾을 수 없습니다."));
        serial.markUnused();
        return "미사용 처리 완료";
    }

    @Transactional
    public List<SerialVo> generateSerials(int count) {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<SerialVo> result = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String serialNum;
            do {
                serialNum = datePrefix + "-DEV-" + String.format("%03d", (int) (Math.random() * 1000));
            } while (serialRepository.existsBySerialNum(serialNum));

            Serial serial = serialRepository.save(Serial.of(serialNum, false));
            result.add(SerialVo.from(serial));
        }
        return result;
    }
}
