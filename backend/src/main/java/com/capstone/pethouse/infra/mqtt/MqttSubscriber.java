package com.capstone.pethouse.infra.mqtt;

import com.capstone.pethouse.infra.mqtt.handler.MqttMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MqttSubscriber {

    private final List<MqttMessageHandler> handlers;

    public MqttSubscriber(List<MqttMessageHandler> handlers) {
        this.handlers = handlers;
        log.info("MQTT Subscriber initialized with {} handler(s): {}",
                handlers.size(),
                handlers.stream().map(h -> h.getClass().getSimpleName()).toList());
    }

    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleMessage(Message<?> message) {
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        String payload = message.getPayload().toString();

        if (topic == null) {
            log.warn("Received MQTT message with null topic, ignoring");
            return;
        }

        log.debug("MQTT Received ← topic={}, payload={}", topic, payload);

        try {
            Long houseId = MqttTopicManager.extractHouseId(topic);
            String category = MqttTopicManager.extractCategory(topic);

            boolean handled = false;
            for (MqttMessageHandler handler : handlers) {
                if (handler.supports(category)) {
                    handler.handle(houseId, category, payload);
                    handled = true;
                }
            }

            if (!handled) {
                log.warn("No handler found for MQTT category: {} (topic: {})", category, topic);
            }
        } catch (NumberFormatException e) {
            log.error("Failed to parse houseId from topic: {}", topic, e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid MQTT topic format: {}", topic, e);
        } catch (Exception e) {
            log.error("Error handling MQTT message on topic {}: {}", topic, e.getMessage(), e);
        }
    }
}
