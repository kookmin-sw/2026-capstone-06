package com.capstone.pethouse.infra.mqtt.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(MqttProperties.class)
@Configuration
public class MqttConfig {

    private final MqttProperties mqttProperties;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setServerURIs(new String[]{mqttProperties.getBroker().getUrl()});
        options.setKeepAliveInterval(mqttProperties.getKeepAliveInterval());
        options.setConnectionTimeout(mqttProperties.getConnectionTimeout());
        options.setAutomaticReconnect(mqttProperties.isAutoReconnect());
        options.setCleanSession(mqttProperties.isCleanSession());

        if (StringUtils.hasText(mqttProperties.getUsername())) {
            options.setUserName(mqttProperties.getUsername());
        }
        if (StringUtils.hasText(mqttProperties.getPassword())) {
            options.setPassword(mqttProperties.getPassword().toCharArray());
        }

        factory.setConnectionOptions(options);
        return factory;
    }

    // === Outbound (Server → Device) ===

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutboundHandler(MqttPahoClientFactory factory) {
        String clientId = mqttProperties.getClient().getId() + "-pub";
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(clientId, factory);
        handler.setAsync(true);
        handler.setDefaultQos(mqttProperties.getDefaultQos());
        handler.setDefaultRetained(false);
        return handler;
    }

    // === Inbound (Device → Server) ===

    @Bean
    public MessageChannel mqttInboundChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public MessageProducer mqttInboundAdapter(MqttPahoClientFactory factory) {
        String clientId = mqttProperties.getClient().getId() + "-sub";
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId, factory);

        DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();
        converter.setPayloadAsBytes(false);
        adapter.setConverter(converter);
        adapter.setQos(mqttProperties.getDefaultQos());
        adapter.setOutputChannel(mqttInboundChannel());

        // 기본 구독 토픽 (와일드카드로 모든 하우스의 모든 메시지 수신)
        adapter.addTopic("pet/+/sensor/data", mqttProperties.getDefaultQos());
        adapter.addTopic("pet/+/device/status", mqttProperties.getDefaultQos());
        adapter.addTopic("pet/+/device/ack", mqttProperties.getDefaultQos());
        adapter.addTopic("pet/+/cam/#", mqttProperties.getDefaultQos());

        log.info("MQTT Inbound Adapter initialized - subscribing to pet/+/sensor/data, pet/+/device/status, pet/+/device/ack, pet/+/cam/#");
        return adapter;
    }
}
