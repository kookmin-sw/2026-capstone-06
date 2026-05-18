package com.capstone.pethouse.domain.sensor.service;

import com.capstone.pethouse.domain.device.entity.Device;
import com.capstone.pethouse.domain.User.entity.User;
import com.capstone.pethouse.domain.device.repository.DeviceRepository;
import com.capstone.pethouse.domain.sensor.dto.SensorResponse;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ChartServiceTest {

    @InjectMocks
    private ChartService chartService;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private InfluxDBClient influxDBClient;

    @Test
    @DisplayName("HOUSE 타입 디바이스 - InfluxDB에서 정상적으로 차트 데이터 반환")
    void chartForHouseDevice() {
        User user = User.ofUser("user01", "pass", "Name", "010");
        Device device = Device.of("DEV001", user, "SN-001", "HOUSE");
        ReflectionTestUtils.setField(device, "seq", 1L);

        SensorResponse res = new SensorResponse(null, "DEV001", 25.0, 60.0, null, 400.0, "20260518120000");

        QueryApi queryApi = mock(QueryApi.class);
        given(influxDBClient.getQueryApi()).willReturn(queryApi);
        given(deviceRepository.findBySerialNum("SN-001")).willReturn(Optional.of(device));
        given(queryApi.query(anyString(), eq(SensorResponse.class))).willReturn(List.of(res));

        List<SensorResponse> result = chartService.getChartData("SN-001", "-24h", "10m");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).deviceId()).isEqualTo("DEV001");
        assertThat(result.get(0).temVal()).isEqualTo(25.0);
        assertThat(result.get(0).humVal()).isEqualTo(60.0);
    }

    @Test
    @DisplayName("시리얼 없으면 빈 리스트 반환")
    void chartNoSerial() {
        given(deviceRepository.findBySerialNum("SN-XXX")).willReturn(Optional.empty());

        assertThat(chartService.getChartData("SN-XXX", "-24h", "10m")).isEmpty();
    }

    @Test
    @DisplayName("serialNum이 null/blank면 빈 리스트 반환")
    void chartNullSerial() {
        assertThat(chartService.getChartData(null, "-24h", "10m")).isEmpty();
        assertThat(chartService.getChartData("", "-24h", "10m")).isEmpty();
    }
}
