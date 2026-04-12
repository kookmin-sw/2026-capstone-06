package com.capstone.pethouse.infra.mqtt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MqttTopicManagerTest {

    @Test
    @DisplayName("houseId 기반 센서 데이터 토픽 생성")
    void sensorDataTopic() {
        assertThat(MqttTopicManager.sensorData(1L)).isEqualTo("pet/1/sensor/data");
        assertThat(MqttTopicManager.sensorData(123L)).isEqualTo("pet/123/sensor/data");
    }

    @Test
    @DisplayName("houseId 기반 디바이스 관련 토픽 생성")
    void deviceTopics() {
        assertThat(MqttTopicManager.deviceStatus(5L)).isEqualTo("pet/5/device/status");
        assertThat(MqttTopicManager.deviceCommand(5L)).isEqualTo("pet/5/device/command");
        assertThat(MqttTopicManager.deviceAck(5L)).isEqualTo("pet/5/device/ack");
    }

    @Test
    @DisplayName("houseId 기반 카메라 토픽 생성")
    void cameraTopics() {
        assertThat(MqttTopicManager.camStream(10L)).isEqualTo("pet/10/cam/stream");
        assertThat(MqttTopicManager.camPtz(10L)).isEqualTo("pet/10/cam/ptz");
    }

    @Test
    @DisplayName("와일드카드 토픽 생성")
    void wildcardTopics() {
        assertThat(MqttTopicManager.allSensorData()).isEqualTo("pet/+/sensor/data");
        assertThat(MqttTopicManager.allDeviceStatus()).isEqualTo("pet/+/device/status");
        assertThat(MqttTopicManager.allDeviceAck()).isEqualTo("pet/+/device/ack");
        assertThat(MqttTopicManager.allCam()).isEqualTo("pet/+/cam/#");
    }

    @Test
    @DisplayName("토픽에서 houseId 추출")
    void extractHouseId() {
        assertThat(MqttTopicManager.extractHouseId("pet/42/sensor/data")).isEqualTo(42L);
        assertThat(MqttTopicManager.extractHouseId("pet/1/device/ack")).isEqualTo(1L);
    }

    @Test
    @DisplayName("토픽에서 카테고리 추출")
    void extractCategory() {
        assertThat(MqttTopicManager.extractCategory("pet/1/sensor/data")).isEqualTo("sensor/data");
        assertThat(MqttTopicManager.extractCategory("pet/1/device/ack")).isEqualTo("device/ack");
        assertThat(MqttTopicManager.extractCategory("pet/1/cam/ptz/move")).isEqualTo("cam/ptz/move");
    }

    @Test
    @DisplayName("잘못된 토픽 형식에서 예외 발생")
    void invalidTopic() {
        assertThatThrownBy(() -> MqttTopicManager.extractHouseId("invalid"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> MqttTopicManager.extractCategory("pet/1"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
