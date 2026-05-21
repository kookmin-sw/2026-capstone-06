package com.capstone.pethouse.infra.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String SENSOR_TOPIC = "pet-house-sensor-data";

    @Bean
    public NewTopic sensorTopic() {
        // 토픽 파티션 수 3, 리플리카 수 1 (로컬 환경 기준)
        return TopicBuilder.name(SENSOR_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
