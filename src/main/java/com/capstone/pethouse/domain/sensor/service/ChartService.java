package com.capstone.pethouse.domain.sensor.service;

import com.capstone.pethouse.domain.device.entity.Device;
import com.capstone.pethouse.domain.device.repository.DeviceRepository;
import com.capstone.pethouse.domain.sensor.dto.SensorResponse;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChartService {

    private final DeviceRepository deviceRepository;
    private final InfluxDBClient influxDBClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String organization;

    @Transactional(readOnly = true)
    public List<SensorResponse> getChartData(String serialNum, String range, String interval) {
        if (serialNum == null || serialNum.isBlank()) {
            return Collections.emptyList();
        }

        Device device = deviceRepository.findBySerialNum(serialNum).orElse(null);
        if (device == null || !"HOUSE".equalsIgnoreCase(device.getDeviceType())) {
            return Collections.emptyList();
        }

        String deviceId = device.getDeviceId();
        List<SensorResponse> result = new ArrayList<>();

        // InfluxDB에서 HOUSE 데이터 조회 (Pivoting 적용하여 각 필드가 컬럼으로 오도록 함)
        String flux = String.format("from(bucket:\"%s\") " +
                "|> range(start: %s) " +
                "|> filter(fn: (r) => r._measurement == \"house_sensor\" and r.deviceId == \"%s\") " +
                "|> aggregateWindow(every: %s, fn: mean, createEmpty: false) " +
                "|> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\") " +
                "|> yield(name: \"mean\")", bucket, range, deviceId, interval);

        try {
            List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, organization);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Double temVal = getDouble(record.getValueByKey("temVal"));
                    Double humVal = getDouble(record.getValueByKey("humVal"));
                    Double coVal = getDouble(record.getValueByKey("coVal"));
                    
                    String regDate = "";
                    if (record.getTime() != null) {
                        regDate = record.getTime().atZone(ZoneId.of("Asia/Seoul"))
                                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    }

                    // seq=null (HOUSE 센서이므로)
                    result.add(new SensorResponse(null, deviceId, temVal, humVal, coVal, regDate));
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch chart data from InfluxDB for deviceId: {}", deviceId, e);
        }

        return result;
    }

    private Double getDouble(Object val) {
        if (val instanceof Number n) {
            return n.doubleValue();
        }
        return null;
    }
}
