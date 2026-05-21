package com.capstone.pethouse.infra.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Kafka 토픽으로 메시지 발행
     * @param topic 발행할 토픽 이름
     * @param payload 전송할 데이터 (DTO 또는 문자열)
     */
    public void sendMessage(String topic, Object payload) {
        log.info("발행 준비 완료: topic='{}', payload='{}'", topic, payload);
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, payload);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("메시지 전송 성공: 토픽=[{}], 오프셋=[{}]", 
                        topic, result.getRecordMetadata().offset());
            } else {
                log.error("메시지 전송 실패: payload=[{}] error=[{}]", payload, ex.getMessage());
            }
        });
    }
}
