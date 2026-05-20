package com.capstone.pethouse.infra.mqtt;

public final class MqttTopicManager {

    private MqttTopicManager() {}

    private static final String ROOT = "pet";

    // === Sensor ===
    public static String sensorData(Long houseId) {
        return ROOT + "/" + houseId + "/sensor/data";
    }

    public static String sensorAlert(Long houseId) {
        return ROOT + "/" + houseId + "/sensor/alert";
    }

    // === Device ===
    public static String deviceStatus(Long houseId) {
        return ROOT + "/" + houseId + "/device/status";
    }

    public static String deviceCommand(Long houseId) {
        return ROOT + "/" + houseId + "/device/command";
    }

    public static String deviceAck(Long houseId) {
        return ROOT + "/" + houseId + "/device/ack";
    }

    // === Camera ===
    public static String camStream(Long houseId) {
        return ROOT + "/" + houseId + "/cam/stream";
    }

    public static String camPtz(Long houseId) {
        return ROOT + "/" + houseId + "/cam/ptz";
    }

    // === Wildcard Patterns (Subscribe용) ===
    public static String allSensorData() {
        return ROOT + "/+/sensor/data";
    }

    public static String allDeviceStatus() {
        return ROOT + "/+/device/status";
    }

    public static String allDeviceAck() {
        return ROOT + "/+/device/ack";
    }

    public static String allCam() {
        return ROOT + "/+/cam/#";
    }

    // === 토픽에서 houseId 추출 ===
    public static Long extractHouseId(String topic) {
        String[] parts = topic.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid topic format: " + topic);
        }
        return Long.parseLong(parts[1]);
    }

    // === 토픽에서 서브 카테고리 추출 (예: "sensor/data", "device/ack") ===
    public static String extractCategory(String topic) {
        String[] parts = topic.split("/");
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid topic format: " + topic);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < parts.length; i++) {
            if (i > 2) sb.append("/");
            sb.append(parts[i]);
        }
        return sb.toString();
    }
}
