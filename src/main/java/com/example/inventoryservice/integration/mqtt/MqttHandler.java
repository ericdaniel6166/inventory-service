package com.example.inventoryservice.integration.mqtt;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageHandler;

@Configuration
@ConditionalOnProperty(name = "spring.mqtt.inbound.enabled", havingValue = "true")
public class MqttHandler {
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> {
            Object topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC); //
            if (ObjectUtils.isNotEmpty(topic)) {
                System.out.println(String.format("topic: %s, payload: %s, headers: %s", topic, message.getPayload(), message.getHeaders()));
            }


        };
    }
}
