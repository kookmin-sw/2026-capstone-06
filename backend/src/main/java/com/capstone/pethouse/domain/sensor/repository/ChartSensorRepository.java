package com.capstone.pethouse.domain.sensor.repository;

import com.capstone.pethouse.domain.sensor.dto.SensorResponse;
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
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository
public class ChartSensorRepository {

    private final InfluxDBClient influxDBClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String organization;

    public List<SensorResponse> getChartData(String deviceId, String range, String interval) {
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
