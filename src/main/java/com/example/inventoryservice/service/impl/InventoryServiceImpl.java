package com.example.inventoryservice.service.impl;

import com.example.inventoryservice.dto.InventoryDto;
import com.example.inventoryservice.dto.OrderRequest;
import com.example.inventoryservice.dto.OrderResponse;
import com.example.inventoryservice.dto.UpdateInventoryDto;
import com.example.inventoryservice.enums.OrderStatus;
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
    public OrderResponse handleOrderPendingOpenFeign(OrderRequest request) {
        var inventoryDtoList = inventoryRepository.findAllByProductIdIn(request.getItemList().stream()
                .map(OrderRequest.Item::getProductId)
                .toList());
        List<UpdateInventoryDto> updateInventoryDtoList = new ArrayList<>();
        List<OrderResponse.Item> processingItemList = new ArrayList<>();
        List<OrderResponse.Item> notAvailableItemList = new ArrayList<>();
        for (var orderPendingItem : request.getItemList()) {
            InventoryDto inventoryDto = inventoryDtoList.stream()
                    .filter(dto -> orderPendingItem.getProductId().equals(dto.getProductId()))
                    .findFirst()
                    .orElse(null);
            var orderDetail = OrderResponse.Item.builder()
                    .productId(orderPendingItem.getProductId())
                    .orderQuantity(orderPendingItem.getOrderQuantity())
                    .build();
            if (inventoryDto == null || orderPendingItem.getOrderQuantity() > inventoryDto.getInventoryQuantity()) {
                orderDetail.setInventoryQuantity(inventoryDto == null ? null : inventoryDto.getInventoryQuantity());
                notAvailableItemList.add(orderDetail);
            } else {
                updateInventoryDtoList.add(new UpdateInventoryDto(inventoryDto.getInventoryId(), orderPendingItem.getOrderQuantity()));
                orderDetail.setProductPrice(inventoryDto.getProductPrice());
                processingItemList.add(orderDetail);
            }
        }
        if (CollectionUtils.isNotEmpty(notAvailableItemList)) {
            return OrderResponse.builder()
                    .orderId(request.getOrderId())
                    .orderStatus(OrderStatus.ITEM_NOT_AVAILABLE)
                    .itemList(notAvailableItemList)
                    .build();
        } else if (CollectionUtils.isNotEmpty(processingItemList)) {
            LocalDateTime now = LocalDateTime.now();
            for (var updateInventoryDto : updateInventoryDtoList) {
                inventoryRepository.update(updateInventoryDto.getInventoryId(), updateInventoryDto.getOrderQuantity(), now);
            }
            return OrderResponse.builder()
                    .orderId(request.getOrderId())
                    .orderStatus(OrderStatus.PROCESSING)
                    .accountNumber(request.getAccountNumber())
                    .itemList(processingItemList)
                    .build();
        }
        return OrderResponse.builder()
                .orderId(request.getOrderId())
                .orderStatus(OrderStatus.ERROR)
                .accountNumber(request.getAccountNumber())
                .build();

    }

    @Transactional
    @Override
    public void handleOrderPendingEvent(OrderPendingEvent event) {
        var inventoryDtoList = inventoryRepository.findAllByProductIdIn(event.getItemList().stream()
                .map(OrderPendingEvent.Item::getProductId).toList());
        List<UpdateInventoryDto> updateInventoryDtoList = new ArrayList<>();
        List<ItemNotAvailableEvent.Item> notAvailableItemList = new ArrayList<>();
        List<OrderProcessingEvent.Item> processingItemList = new ArrayList<>();
        for (var orderPendingItem : event.getItemList()) {
            InventoryDto inventoryDto = inventoryDtoList.stream()
                    .filter(dto -> orderPendingItem.getProductId().equals(dto.getProductId()))
                    .findFirst()
                    .orElse(null);
            if (inventoryDto == null || orderPendingItem.getOrderQuantity() > inventoryDto.getInventoryQuantity()) {
                notAvailableItemList.add(ItemNotAvailableEvent.Item.builder()
                        .productId(orderPendingItem.getProductId())
                        .orderQuantity(orderPendingItem.getOrderQuantity())
                        .inventoryQuantity(inventoryDto == null ? null : inventoryDto.getInventoryQuantity())
                        .build());
            } else {
                updateInventoryDtoList.add(new UpdateInventoryDto(inventoryDto.getInventoryId(), orderPendingItem.getOrderQuantity()));
                processingItemList.add(OrderProcessingEvent.Item.builder()
                        .productId(orderPendingItem.getProductId())
                        .orderQuantity(orderPendingItem.getOrderQuantity())
                        .productPrice(inventoryDto.getProductPrice())
                        .build());
            }

        }
        if (CollectionUtils.isNotEmpty(notAvailableItemList)) {
            var itemNotAvailableEvent = ItemNotAvailableEvent.builder()
                    .orderId(event.getOrderId())
                    .itemList(notAvailableItemList)
                    .build();
            log.info("send itemNotAvailableEvent {}", itemNotAvailableEvent);
            kafkaTemplate.send(kafkaProducerProperties.getOrderItemNotAvailableTopicName(), Event.builder()
                    .payload(itemNotAvailableEvent)
                    .build());
        } else if (CollectionUtils.isNotEmpty(processingItemList)) {
            LocalDateTime now = LocalDateTime.now();
            for (var updateInventoryDto : updateInventoryDtoList) {
                inventoryRepository.update(updateInventoryDto.getInventoryId(), updateInventoryDto.getOrderQuantity(), now);
            }
            var orderProcessingEvent = OrderProcessingEvent.builder()
                    .orderId(event.getOrderId())
                    .accountNumber(event.getAccountNumber())
                    .itemList(processingItemList)
                    .build();
            log.info("send orderProcessingEvent {}", orderProcessingEvent);
            kafkaTemplate.send(kafkaProducerProperties.getOrderProcessingTopicName(), Event.builder()
                    .payload(orderProcessingEvent)
                    .build());
        }
    }
}

