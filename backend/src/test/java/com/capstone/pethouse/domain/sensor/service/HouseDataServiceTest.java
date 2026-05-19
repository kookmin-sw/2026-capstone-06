package com.capstone.pethouse.domain.sensor.service;

import com.capstone.pethouse.domain.sensor.dto.SensorResponse;
import com.capstone.pethouse.domain.sensor.dto.HouseDataRequest;
import com.capstone.pethouse.domain.sensor.influx.InfluxWriter;
import com.capstone.pethouse.domain.sensor.websocket.SensorPushService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HouseDataServiceTest {

    @InjectMocks
    private HouseDataService houseDataService;

    @Mock
    private InfluxWriter influxWriter;

    @Mock
    private SensorPushService sensorPushService;

    @Test
    @DisplayName("하우스 데이터 등록 - Influx write + WebSocket push")
    void createSuccess() {
        HouseDataRequest request = new HouseDataRequest("DEV001", 25.3, 60.0, 410.0);

        SensorResponse response = houseDataService.create(request);

        assertThat(response.deviceId()).isEqualTo("DEV001");
        assertThat(response.temVal()).isEqualTo(25.3);
        verify(influxWriter).writeHouse("DEV001", 25.3, 60.0, 410.0);
        verify(sensorPushService).pushHouse(any(SensorResponse.class));
    }

    @Test
    @DisplayName("등록 실패 - device_id 누락")
    void createFailNoDeviceId() {
        HouseDataRequest request = new HouseDataRequest(null, 25.3, 60.0, 410.0);

        assertThatThrownBy(() -> houseDataService.create(request))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(influxWriter, sensorPushService);
    }
}
