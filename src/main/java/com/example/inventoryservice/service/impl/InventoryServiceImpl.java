package com.example.inventoryservice.service.impl;

import com.example.inventoryservice.dto.InventoryDto;
import com.example.inventoryservice.dto.UpdateInventoryDto;
import com.example.inventoryservice.integration.kafka.config.KafkaProducerProperties;
import com.example.inventoryservice.integration.kafka.event.ItemNotAvailableEvent;
import com.example.inventoryservice.integration.kafka.event.OrderPendingEvent;
import com.example.inventoryservice.integration.kafka.event.OrderProcessingEvent;
import com.example.inventoryservice.repository.InventoryRepository;
import com.example.inventoryservice.service.InventoryService;
import com.example.springbootmicroservicesframework.kafka.event.Event;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryServiceImpl implements InventoryService {

    final InventoryRepository inventoryRepository;

    final KafkaTemplate<String, Object> kafkaTemplate;

    final KafkaProducerProperties kafkaProducerProperties;

    @Transactional
    @Override
    public void handleOrderPendingEvent(OrderPendingEvent event) {
        var inventoryDtoList = inventoryRepository.findAllByProductIdIn(event.getOrderPendingItemList().stream()
                .map(OrderPendingEvent.OrderPendingItem::getProductId).toList());
        List<UpdateInventoryDto> updateInventoryDtoList = new ArrayList<>();
        List<ItemNotAvailableEvent.ItemNotAvailable> itemNotAvailableList = new ArrayList<>();
        List<OrderProcessingEvent.OrderProcessingItem> orderProcessingItemList = new ArrayList<>();
        for (var orderPendingItem : event.getOrderPendingItemList()) {
            InventoryDto inventoryDto = inventoryDtoList.stream()
                    .filter(dto -> orderPendingItem.getProductId().equals(dto.getProductId()))
                    .findFirst()
                    .orElse(null);
            if (inventoryDto == null || orderPendingItem.getOrderQuantity() > inventoryDto.getInventoryQuantity()) {
                itemNotAvailableList.add(ItemNotAvailableEvent.ItemNotAvailable.builder()
                        .productId(orderPendingItem.getProductId())
                        .orderQuantity(orderPendingItem.getOrderQuantity())
                        .inventoryQuantity(inventoryDto == null ? null : inventoryDto.getInventoryQuantity())
                        .build());
            } else {
                updateInventoryDtoList.add(new UpdateInventoryDto(inventoryDto.getInventoryId(), orderPendingItem.getOrderQuantity()));
                orderProcessingItemList.add(OrderProcessingEvent.OrderProcessingItem.builder()
                        .productId(orderPendingItem.getProductId())
                        .orderQuantity(orderPendingItem.getOrderQuantity())
                        .productPrice(inventoryDto.getProductPrice())
                        .build());
            }

        }
        if (CollectionUtils.isNotEmpty(itemNotAvailableList)) {
            var itemNotAvailableEvent = ItemNotAvailableEvent.builder()
                    .orderId(event.getOrderId())
                    .itemNotAvailableList(itemNotAvailableList)
                    .build();
            log.info("send itemNotAvailableEvent {}", itemNotAvailableEvent);
            kafkaTemplate.send(kafkaProducerProperties.getOrderItemNotAvailableTopicName(), Event.builder()
                    .payload(itemNotAvailableEvent)
                    .build());
        } else if (CollectionUtils.isNotEmpty(orderProcessingItemList)) {
            LocalDateTime now = LocalDateTime.now();
            for (var updateInventoryDto : updateInventoryDtoList) {
                inventoryRepository.update(updateInventoryDto.getInventoryId(), updateInventoryDto.getOrderQuantity(), now);
            }
            var orderProcessingEvent = OrderProcessingEvent.builder()
                    .orderId(event.getOrderId())
                    .accountNumber(event.getAccountNumber())
                    .orderProcessingItemList(orderProcessingItemList)
                    .build();
            log.info("send orderProcessingEvent {}", orderProcessingEvent);
            kafkaTemplate.send(kafkaProducerProperties.getOrderProcessingTopicName(), Event.builder()
                    .payload(orderProcessingEvent)
                    .build());
        }
    }
}

