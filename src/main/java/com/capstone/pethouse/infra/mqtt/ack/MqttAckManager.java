package com.capstone.pethouse.infra.mqtt.ack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Component
public class MqttAckManager {

    private static final long DEFAULT_TIMEOUT_SECONDS = 10;

    private final ConcurrentMap<String, CompletableFuture<AckResult>> pendingAcks = new ConcurrentHashMap<>();

    public String generateCommandId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public CompletableFuture<AckResult> register(String commandId) {
        CompletableFuture<AckResult> future = new CompletableFuture<>();
        pendingAcks.put(commandId, future);

        // 타임아웃 후 자동 제거
        CompletableFuture.delayedExecutor(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .execute(() -> {
                    CompletableFuture<AckResult> pending = pendingAcks.remove(commandId);
                    if (pending != null && !pending.isDone()) {
                        pending.complete(AckResult.timeout());
                        log.warn("ACK timeout for commandId={}", commandId);
                    }
                });

        return future;
    }

    public CompletableFuture<AckResult> register(String commandId, long timeoutSeconds) {
        CompletableFuture<AckResult> future = new CompletableFuture<>();
        pendingAcks.put(commandId, future);

        CompletableFuture.delayedExecutor(timeoutSeconds, TimeUnit.SECONDS)
                .execute(() -> {
                    CompletableFuture<AckResult> pending = pendingAcks.remove(commandId);
                    if (pending != null && !pending.isDone()) {
                        pending.complete(AckResult.timeout());
                        log.warn("ACK timeout for commandId={}", commandId);
                    }
                });

        return future;
    }

    public void complete(String commandId, boolean success, String message) {
        CompletableFuture<AckResult> future = pendingAcks.remove(commandId);
        if (future != null) {
            AckResult result = success ? AckResult.success(message) : AckResult.failure(message);
            future.complete(result);
            log.debug("ACK completed — commandId={}, success={}", commandId, success);
        } else {
            log.warn("ACK received for unknown/expired commandId={}", commandId);
        }
    }

    public int pendingCount() {
        return pendingAcks.size();
    }
}
