package com.example.inventoryservice.integration.kafka.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class KafkaProducerProperties {

    @Value("${spring.kafka.producers.order-item-not-available.topic-name}")
    String orderItemNotAvailableTopicName;

    @Value("${spring.kafka.producers.order-processing.topic-name}")
    String orderProcessingTopicName;

}
