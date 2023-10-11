package com.example.inventoryservice.integration.kafka;

import com.example.inventoryservice.integration.kafka.event.OrderPendingEvent;
import com.example.inventoryservice.service.InventoryService;
import com.example.springbootmicroservicesframework.kafka.event.Event;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class KafkaConsumer {

    final ModelMapper modelMapper;

    final InventoryService inventoryService;

    @KafkaListener(topics = "${spring.kafka.consumers.order-pending.topic-name}",
            groupId = "${spring.kafka.consumers.order-pending.group-id}",
            containerFactory = "orderPendingKafkaListenerContainerFactory",
            concurrency = "${spring.kafka.consumers.order-pending.properties.concurrency}"
    )
    public void handleOrderPending(Event event) {
        var orderPendingEvent = modelMapper.map(event.getPayload(), OrderPendingEvent.class);
        log.info("handle orderPendingEvent {}", orderPendingEvent);
        inventoryService.handleOrderPendingEvent(orderPendingEvent);
    }

}
