package com.capstone.pethouse.domain.sensor.service;

import com.capstone.pethouse.domain.sensor.dto.DataVo;
import com.capstone.pethouse.domain.sensor.dto.HouseDataRequest;
import com.capstone.pethouse.domain.sensor.entity.HouseData;
import com.capstone.pethouse.domain.sensor.influx.InfluxWriter;
import com.capstone.pethouse.domain.sensor.repository.HouseDataRepository;
import com.capstone.pethouse.domain.sensor.websocket.SensorPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class HouseDataService {

    private final HouseDataRepository houseDataRepository;
    private final InfluxWriter influxWriter;
    private final SensorPushService sensorPushService;

    @Transactional(readOnly = true)
    public Page<DataVo> getList(int pageNum, int pageSize, String searchQuery) {
        PageRequest pageRequest = PageRequest.of(Math.max(pageNum - 1, 0), pageSize);
        return houseDataRepository.findAllWithSearch(searchQuery, pageRequest).map(DataVo::fromHouse);
    }

    @Transactional(readOnly = true)
    public DataVo get(Long seq) {
        HouseData data = houseDataRepository.findById(seq)
                .orElseThrow(() -> new IllegalArgumentException("하우스 데이터를 찾을 수 없습니다."));
        return DataVo.fromHouse(data);
    }

    /**
     * HTTP/MQTT 양쪽에서 호출. RDB 저장 + InfluxDB write + WebSocket push.
     */
    @Transactional
    public DataVo create(HouseDataRequest request) {
        if (request.deviceId() == null || request.deviceId().isBlank()) {
            throw new IllegalArgumentException("device_id는 필수입니다.");
        }

        HouseData saved = houseDataRepository.save(
                HouseData.of(request.deviceId(), request.temVal(), request.humVal(), request.coVal())
        );

        DataVo vo = DataVo.fromHouse(saved);

        // InfluxDB 시계열 저장
        influxWriter.writeHouse(request.deviceId(), request.temVal(), request.humVal(), request.coVal());

        // WebSocket 실시간 푸시
        sensorPushService.pushHouse(vo);

        return vo;
    }

    @Transactional
    public DataVo update(Long seq, HouseDataRequest request) {
        HouseData data = houseDataRepository.findById(seq)
                .orElseThrow(() -> new IllegalArgumentException("하우스 데이터를 찾을 수 없습니다."));
        data.update(request.deviceId(), request.temVal(), request.humVal(), request.coVal());
        return DataVo.fromHouse(data);
    }

    @Transactional
    public void delete(Long seq) {
        if (!houseDataRepository.existsById(seq)) {
            throw new IllegalArgumentException("하우스 데이터를 찾을 수 없습니다.");
        }
        houseDataRepository.deleteById(seq);
    }
}
