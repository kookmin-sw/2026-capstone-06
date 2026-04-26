package com.capstone.pethouse.domain.dashboard.repository;

import com.capstone.pethouse.domain.dashboard.dto.DashboardResponse.SensorDataRes;
import com.capstone.pethouse.domain.dashboard.dto.DashboardResponse.SensorHistoryRes;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
                    lastUpdate,
                    null, null, null, null, null, null, null, null, null, null // Filled by Service layer later
            );
        } catch (Exception e) {
            log.error("Failed to fetch latest sensor data from InfluxDB", e);
            return null;
        }
    }

    // Fetch Sensor Data History for a given deviceId
    public List<SensorHistoryRes> getSensorDataHistory(String deviceId) {
        // Here we query historical data, maybe aggegating or just pulling raw points
        String flux = String.format("from(bucket:\"%s\") " +
                "|> range(start: -7d) " +
                "|> filter(fn: (r) => r._measurement == \"sensor\" and r.deviceId == \"%s\") " +
                "|> pivot(rowKey:[\"_time\"], columnKey: [\"_field\"], valueColumn: \"_value\") " +
                "|> map(fn: (r) => ({ r with seq: 1 })) " + // Dummy seq since Influx doesn't have auto-inc PK
                "|> sort(columns: [\"_time\"], desc: true) " +
                "|> limit(n: 100)", bucket, deviceId);

        List<SensorHistoryRes> history = new ArrayList<>();
        try {
            List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, organization);
            long seqCounter = 1;
            
            for (FluxTable table : tables) {
                for (FluxRecord r : table.getRecords()) {
                    String timeStr = r.getTime().atZone(ZoneId.of("Asia/Seoul"))
                            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

                    history.add(new SensorHistoryRes(
                            seqCounter++,
                            deviceId,
                            getDouble(r.getValueByKey("temperature")),
                            getDouble(r.getValueByKey("humidity")),
                            getDouble(r.getValueByKey("heartRate")),
                            getDouble(r.getValueByKey("co2")),
                            timeStr
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch sensor history from InfluxDB", e);
        }

        return history;
    }

    private Double getDouble(Object val) {
        if (val instanceof Number n) {
            return n.doubleValue();
        }
        return null;
    }
}
