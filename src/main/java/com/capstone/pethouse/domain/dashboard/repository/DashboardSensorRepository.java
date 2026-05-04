package com.capstone.pethouse.domain.dashboard.repository;

import com.capstone.pethouse.domain.dashboard.dto.DashboardResponse.SensorDataRes;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Repository
public class DashboardSensorRepository {

    private final InfluxDBClient influxDBClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String organization;

    // Fetch Latest Sensor Data for a given deviceId
    public SensorDataRes getLatestSensorData(String deviceId) {
        String flux = String.format("from(bucket:\"%s\") " +
                "|> range(start: -30d) " +
                "|> filter(fn: (r) => r._measurement == \"sensor\" and r.deviceId == \"%s\") " +
                "|> last()", bucket, deviceId);

        try {
            List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, organization);
            if (tables.isEmpty()) return null;

            Map<String, Object> fields = new HashMap<>();
            String lastUpdate = null;

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    fields.put(record.getField(), record.getValue());
                    if (lastUpdate == null && record.getTime() != null) {
                        lastUpdate = record.getTime().atZone(ZoneId.of("Asia/Seoul"))
                                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    }
                }
            }

            if (fields.isEmpty()) return null;

            return new SensorDataRes(
                    deviceId,
                    getDouble(fields.get("temperature")),
                    getDouble(fields.get("humidity")),
                    getDouble(fields.get("heartRate")),
                    getDouble(fields.get("co2")),
                    lastUpdate
            );
        } catch (Exception e) {
            log.error("Failed to fetch latest sensor data from InfluxDB", e);
            return null;
        }
    }

    private Double getDouble(Object val) {
        if (val instanceof Number n) {
            return n.doubleValue();
        }
        return null;
    }
}
