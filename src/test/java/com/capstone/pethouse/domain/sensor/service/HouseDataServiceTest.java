package com.capstone.pethouse.domain.sensor.service;

import com.capstone.pethouse.domain.sensor.dto.DataVo;
import com.capstone.pethouse.domain.sensor.dto.HouseDataRequest;
import com.capstone.pethouse.domain.sensor.entity.HouseData;
import com.capstone.pethouse.domain.sensor.influx.InfluxWriter;
import com.capstone.pethouse.domain.sensor.repository.HouseDataRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HouseDataServiceTest {

    @InjectMocks
    private HouseDataService houseDataService;

    @Mock
    private HouseDataRepository houseDataRepository;

    @Mock
    private InfluxWriter influxWriter;

    @Mock
    private SensorPushService sensorPushService;

    private HouseData createHouseData() {
        HouseData data = HouseData.of("DEV001", 25.3, 60.0, 410.0);
        ReflectionTestUtils.setField(data, "seq", 1L);
        ReflectionTestUtils.setField(data, "regDate", LocalDateTime.now());
        return data;
    }

    @Test
    @DisplayName("하우스 데이터 등록 - RDB 저장 + Influx write + WebSocket push")
    void createSuccess() {
        HouseDataRequest request = new HouseDataRequest("DEV001", 25.3, 60.0, 410.0);
        HouseData saved = createHouseData();

        given(houseDataRepository.save(any(HouseData.class))).willReturn(saved);

        DataVo response = houseDataService.create(request);

        assertThat(response.deviceId()).isEqualTo("DEV001");
        assertThat(response.temVal()).isEqualTo(25.3);
        verify(influxWriter).writeHouse("DEV001", 25.3, 60.0, 410.0);
        verify(sensorPushService).pushHouse(any(DataVo.class));
    }

    @Test
    @DisplayName("등록 실패 - device_id 누락")
    void createFailNoDeviceId() {
        HouseDataRequest request = new HouseDataRequest(null, 25.3, 60.0, 410.0);

        assertThatThrownBy(() -> houseDataService.create(request))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(influxWriter, sensorPushService);
    }

    @Test
    @DisplayName("단일 조회 성공")
    void getSuccess() {
        HouseData data = createHouseData();
        given(houseDataRepository.findById(1L)).willReturn(Optional.of(data));

        DataVo response = houseDataService.get(1L);

        assertThat(response.seq()).isEqualTo(1L);
    }

    @Test
    @DisplayName("단일 조회 실패")
    void getNotFound() {
        given(houseDataRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> houseDataService.get(999L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("수정 성공")
    void updateSuccess() {
        HouseData data = createHouseData();
        HouseDataRequest request = new HouseDataRequest("DEV001", 30.0, 65.0, 500.0);

        given(houseDataRepository.findById(1L)).willReturn(Optional.of(data));

        houseDataService.update(1L, request);

        assertThat(data.getTemVal()).isEqualTo(30.0);
        assertThat(data.getCoVal()).isEqualTo(500.0);
    }

    @Test
    @DisplayName("삭제 성공")
    void deleteSuccess() {
        given(houseDataRepository.existsById(1L)).willReturn(true);

        houseDataService.delete(1L);

        verify(houseDataRepository).deleteById(1L);
    }

    @Test
    @DisplayName("삭제 실패 - 존재하지 않음")
    void deleteNotFound() {
        given(houseDataRepository.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> houseDataService.delete(999L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
