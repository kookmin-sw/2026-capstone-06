package com.capstone.pethouse.domain.sensor.service;

import com.capstone.pethouse.domain.sensor.dto.DataVo;
import com.capstone.pethouse.domain.sensor.dto.NeckDataRequest;
import com.capstone.pethouse.domain.sensor.entity.NeckData;
import com.capstone.pethouse.domain.sensor.influx.InfluxWriter;
import com.capstone.pethouse.domain.sensor.repository.NeckDataRepository;
import com.capstone.pethouse.domain.sensor.websocket.SensorPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class NeckDataService {

    private final NeckDataRepository neckDataRepository;
    private final InfluxWriter influxWriter;
    private final SensorPushService sensorPushService;

    @Transactional(readOnly = true)
    public Page<DataVo> getList(int pageNum, int pageSize, String searchQuery) {
        PageRequest pageRequest = PageRequest.of(Math.max(pageNum - 1, 0), pageSize);
        return neckDataRepository.findAllWithSearch(searchQuery, pageRequest).map(DataVo::fromNeck);
    }

    @Transactional(readOnly = true)
    public DataVo get(Long seq) {
        NeckData data = neckDataRepository.findById(seq)
                .orElseThrow(() -> new IllegalArgumentException("목걸이 데이터를 찾을 수 없습니다."));
        return DataVo.fromNeck(data);
    }

    @Transactional
    public DataVo create(NeckDataRequest request) {
        if (request.deviceId() == null || request.deviceId().isBlank()) {
            throw new IllegalArgumentException("device_id는 필수입니다.");
        }

        NeckData saved = neckDataRepository.save(
                NeckData.of(request.deviceId(), request.temVal(), request.heartVal(), request.coVal())
        );

        DataVo vo = DataVo.fromNeck(saved);

        influxWriter.writeNeck(request.deviceId(), request.temVal(), request.heartVal(), request.coVal());
        sensorPushService.pushNeck(vo);

        return vo;
    }

    @Transactional
    public DataVo update(Long seq, NeckDataRequest request) {
        NeckData data = neckDataRepository.findById(seq)
                .orElseThrow(() -> new IllegalArgumentException("목걸이 데이터를 찾을 수 없습니다."));
        data.update(request.deviceId(), request.temVal(), request.heartVal(), request.coVal());
        return DataVo.fromNeck(data);
    }

    @Transactional
    public void delete(Long seq) {
        if (!neckDataRepository.existsById(seq)) {
            throw new IllegalArgumentException("목걸이 데이터를 찾을 수 없습니다.");
        }
        neckDataRepository.deleteById(seq);
    }
}
