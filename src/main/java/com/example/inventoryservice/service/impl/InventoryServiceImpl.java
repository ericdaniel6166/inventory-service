package com.example.inventoryservice.service.impl;

import com.example.inventoryservice.config.kafka.KafkaProducerProperties;
import com.example.inventoryservice.dto.InventoryDto;
import com.example.inventoryservice.dto.UpdateInventoryDto;
import com.example.inventoryservice.integration.event.ItemNotAvailableEvent;
import com.example.inventoryservice.integration.event.OrderPendingEvent;
import com.example.inventoryservice.repository.InventoryRepository;
import com.example.inventoryservice.service.InventoryService;
import com.example.springbootmicroservicesframework.dto.Event;
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
        var inventoryDtoList = inventoryRepository.findAllByProductIdIn(event.getOrderItemList().stream()
                .map(OrderPendingEvent.OrderItemDto::getProductId).toList());
        List<UpdateInventoryDto> updateInventoryDtoList = new ArrayList<>();
        List<ItemNotAvailableEvent.ItemNotAvailable> itemNotAvailableList = new ArrayList<>();
        for (var orderItemDto : event.getOrderItemList()) {
            InventoryDto inventoryDto = inventoryDtoList.stream()
                    .filter(dto -> orderItemDto.getProductId().equals(dto.getProductId()))
                    .findFirst()
                    .orElse(null);
            if (inventoryDto == null || orderItemDto.getQuantity() > inventoryDto.getInventoryQuantity()) {
                itemNotAvailableList.add(ItemNotAvailableEvent.ItemNotAvailable.builder()
                        .productId(orderItemDto.getProductId())
                        .orderQuantity(orderItemDto.getQuantity())
                        .inventoryQuantity(inventoryDto == null ? null : inventoryDto.getInventoryQuantity())
                        .build());
            } else {
                updateInventoryDtoList.add(new UpdateInventoryDto(inventoryDto.getInventoryId(), orderItemDto.getQuantity()));
            }

        }
        if (CollectionUtils.isEmpty(itemNotAvailableList)) {
            LocalDateTime now = LocalDateTime.now();
            for (var updateInventoryDto : updateInventoryDtoList) {
                inventoryRepository.update(updateInventoryDto.getInventoryId(), updateInventoryDto.getOrderQuantity(), now);
            }
        } else {
            var itemNotAvailableEvent = ItemNotAvailableEvent.builder()
                    .orderId(event.getOrderId())
                    .itemNotAvailableList(itemNotAvailableList)
                    .build();
            log.info("send itemNotAvailableEvent {}", itemNotAvailableEvent);
            kafkaTemplate.send(kafkaProducerProperties.getOrderItemNotAvailableTopicName(), Event.builder()
                    .payload(itemNotAvailableEvent)
                    .build());
        }
    }
}

