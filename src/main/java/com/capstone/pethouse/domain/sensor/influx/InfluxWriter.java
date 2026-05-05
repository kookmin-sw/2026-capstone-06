package com.capstone.pethouse.domain.sensor.influx;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
@Component
public class InfluxWriter {

    public static final String MEASUREMENT_HOUSE = "house_sensor";
    public static final String MEASUREMENT_NECK = "neck_sensor";

    private final InfluxDBClient influxDBClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String influxOrg;

    public void writeHouse(String deviceId, Double temVal, Double humVal, Double coVal) {
        Point point = Point.measurement(MEASUREMENT_HOUSE)
                .addTag("deviceId", deviceId)
                .time(Instant.now(), WritePrecision.MS);

        if (temVal != null) point.addField("temVal", temVal);
        if (humVal != null) point.addField("humVal", humVal);
        if (coVal != null) point.addField("coVal", coVal);

        try {
            WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
            writeApi.writePoint(bucket, influxOrg, point);
        } catch (Exception e) {
            log.warn("InfluxDB write failed (house) for {}: {}", deviceId, e.getMessage());
        }
    }

    public void writeNeck(String deviceId, Double temVal, Double heartVal, Double coVal) {
        Point point = Point.measurement(MEASUREMENT_NECK)
                .addTag("deviceId", deviceId)
                .time(Instant.now(), WritePrecision.MS);

        if (temVal != null) point.addField("temVal", temVal);
        if (heartVal != null) point.addField("heartVal", heartVal);
        if (coVal != null) point.addField("coVal", coVal);

        try {
            WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
            writeApi.writePoint(bucket, influxOrg, point);
        } catch (Exception e) {
            log.warn("InfluxDB write failed (neck) for {}: {}", deviceId, e.getMessage());
        }
    }
}
