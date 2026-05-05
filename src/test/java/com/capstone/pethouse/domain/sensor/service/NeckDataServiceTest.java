package com.capstone.pethouse.domain.sensor.service;

import com.capstone.pethouse.domain.sensor.dto.DataVo;
import com.capstone.pethouse.domain.sensor.dto.NeckDataRequest;
import com.capstone.pethouse.domain.sensor.entity.NeckData;
import com.capstone.pethouse.domain.sensor.influx.InfluxWriter;
import com.capstone.pethouse.domain.sensor.repository.NeckDataRepository;
import com.capstone.pethouse.domain.sensor.websocket.SensorPushService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NeckDataServiceTest {

    @InjectMocks
    private NeckDataService neckDataService;

    @Mock
    private NeckDataRepository neckDataRepository;

    @Mock
    private InfluxWriter influxWriter;

    @Mock
    private SensorPushService sensorPushService;

    private NeckData createNeckData() {
        NeckData data = NeckData.of("DEV002", 18.0, 64.0, 404.0);
        ReflectionTestUtils.setField(data, "seq", 2L);
        ReflectionTestUtils.setField(data, "regDate", LocalDateTime.now());
        return data;
    }

    @Test
    @DisplayName("목걸이 데이터 등록 - RDB + Influx + WebSocket")
    void createSuccess() {
        NeckDataRequest request = new NeckDataRequest("DEV002", 18.0, 64.0, 404.0);
        NeckData saved = createNeckData();

        given(neckDataRepository.save(any(NeckData.class))).willReturn(saved);

        DataVo response = neckDataService.create(request);

        assertThat(response.deviceId()).isEqualTo("DEV002");
        assertThat(response.heartVal()).isEqualTo(64.0);
        verify(influxWriter).writeNeck("DEV002", 18.0, 64.0, 404.0);
        verify(sensorPushService).pushNeck(any(DataVo.class));
    }

    @Test
    @DisplayName("단일 조회")
    void getSuccess() {
        NeckData data = createNeckData();
        given(neckDataRepository.findById(2L)).willReturn(Optional.of(data));

        DataVo response = neckDataService.get(2L);

        assertThat(response.heartVal()).isEqualTo(64.0);
    }

    @Test
    @DisplayName("수정 성공")
    void updateSuccess() {
        NeckData data = createNeckData();
        NeckDataRequest request = new NeckDataRequest("DEV002", 20.0, 70.0, 410.0);

        given(neckDataRepository.findById(2L)).willReturn(Optional.of(data));

        neckDataService.update(2L, request);

        assertThat(data.getHeartVal()).isEqualTo(70.0);
    }

    @Test
    @DisplayName("삭제 실패")
    void deleteFail() {
        given(neckDataRepository.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> neckDataService.delete(999L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
