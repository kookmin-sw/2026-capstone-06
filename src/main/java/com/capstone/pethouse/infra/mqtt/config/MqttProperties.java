package com.capstone.pethouse.infra.mqtt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {

    private Broker broker = new Broker();
    private Client client = new Client();
    private String username;
    private String password;
    private int defaultQos = 1;
    private int keepAliveInterval = 60;
    private int connectionTimeout = 30;
    private boolean autoReconnect = true;
    private boolean cleanSession = true;

    @Getter
    @Setter
    public static class Broker {
        private String url = "tcp://localhost:1883";
    }

    @Getter
    @Setter
    public static class Client {
        private String id = "pet-house-server";
    }
}
