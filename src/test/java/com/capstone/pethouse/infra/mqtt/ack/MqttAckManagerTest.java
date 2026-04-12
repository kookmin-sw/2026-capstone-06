package com.capstone.pethouse.infra.mqtt.ack;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class MqttAckManagerTest {

    @Test
    @DisplayName("commandId 생성 시 유니크한 값 반환")
    void generateUniqueCommandIds() {
        MqttAckManager manager = new MqttAckManager();
        String id1 = manager.generateCommandId();
        String id2 = manager.generateCommandId();

        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1).hasSize(8);
    }

    @Test
    @DisplayName("ACK 등록 후 complete 호출 시 성공 결과 반환")
    void registerAndComplete() throws Exception {
        MqttAckManager manager = new MqttAckManager();
        String commandId = manager.generateCommandId();

        CompletableFuture<AckResult> future = manager.register(commandId);
        assertThat(manager.pendingCount()).isEqualTo(1);

        manager.complete(commandId, true, "OK");

        AckResult result = future.get(1, TimeUnit.SECONDS);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("OK");
        assertThat(manager.pendingCount()).isZero();
    }

    @Test
    @DisplayName("ACK 등록 후 실패 complete 호출 시 실패 결과 반환")
    void registerAndCompleteFail() throws Exception {
        MqttAckManager manager = new MqttAckManager();
        String commandId = manager.generateCommandId();

        CompletableFuture<AckResult> future = manager.register(commandId);
        manager.complete(commandId, false, "Device busy");

        AckResult result = future.get(1, TimeUnit.SECONDS);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Device busy");
    }

    @Test
    @DisplayName("타임아웃 시 timeout 결과 반환")
    void timeout() throws Exception {
        MqttAckManager manager = new MqttAckManager();
        String commandId = manager.generateCommandId();

        CompletableFuture<AckResult> future = manager.register(commandId, 1); // 1초 타임아웃

        AckResult result = future.get(3, TimeUnit.SECONDS);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("ACK timeout");
    }

    @Test
    @DisplayName("존재하지 않는 commandId complete 시 무시")
    void completeUnknown() {
        MqttAckManager manager = new MqttAckManager();
        // 예외 없이 무시되어야 함
        manager.complete("unknown-id", true, "OK");
        assertThat(manager.pendingCount()).isZero();
    }

    @Test
    @DisplayName("AckResult 정적 팩토리 메서드")
    void ackResultFactoryMethods() {
        AckResult success = AckResult.success("done");
        assertThat(success.isSuccess()).isTrue();
        assertThat(success.getMessage()).isEqualTo("done");

        AckResult failure = AckResult.failure("error");
        assertThat(failure.isSuccess()).isFalse();

        AckResult timeout = AckResult.timeout();
        assertThat(timeout.isSuccess()).isFalse();
        assertThat(timeout.getMessage()).isEqualTo("ACK timeout");
    }
}
