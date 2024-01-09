package com.example.inventoryservice.service.impl;

import com.example.inventoryservice.dto.InventoryDto;
import com.example.inventoryservice.dto.OrderPendingRequest;
import com.example.inventoryservice.dto.OrderPendingResponse;
import com.example.inventoryservice.dto.UpdateInventoryDto;
import com.example.inventoryservice.enums.OrderStatus;
import com.example.inventoryservice.integration.kafka.config.KafkaProducerProperties;
import com.example.inventoryservice.integration.kafka.event.ItemNotAvailableEvent;
import com.example.inventoryservice.integration.kafka.event.OrderPendingEvent;
import com.example.inventoryservice.integration.kafka.event.OrderProcessingEvent;
import com.example.inventoryservice.repository.InventoryRepository;
import com.example.inventoryservice.service.InventoryService;
import com.example.springbootmicroservicesframework.integration.kafka.event.Event;
import com.example.springbootmicroservicesframework.utils.AppSecurityUtils;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryServiceImpl implements InventoryService {

    InventoryRepository inventoryRepository;

    KafkaTemplate<String, Object> kafkaTemplate;

    KafkaProducerProperties kafkaProducerProperties;

    @Transactional
    @Override
    public OrderPendingResponse handleOrderPendingOpenFeign(OrderPendingRequest request) {
        var inventoryDtoList = inventoryRepository.findAllByProductIdIn(request.getItemList().stream()
                .map(OrderPendingRequest.Item::getProductId)
                .toList());
        List<UpdateInventoryDto> updateInventoryDtoList = new ArrayList<>();
        List<OrderPendingResponse.Item> processingItemList = new ArrayList<>();
        List<OrderPendingResponse.Item> notAvailableItemList = new ArrayList<>();
        for (var orderPendingItem : request.getItemList()) {
            InventoryDto inventoryDto = inventoryDtoList.stream()
                    .filter(dto -> orderPendingItem.getProductId().equals(dto.getProductId()))
                    .findFirst()
                    .orElse(null);
            var orderDetail = OrderPendingResponse.Item.builder()
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
            return OrderPendingResponse.builder()
                    .orderId(request.getOrderId())
                    .orderStatus(OrderStatus.ITEM_NOT_AVAILABLE)
                    .itemList(notAvailableItemList)
                    .build();
        } else if (CollectionUtils.isNotEmpty(processingItemList)) {
            LocalDateTime now = LocalDateTime.now();
            for (var updateInventoryDto : updateInventoryDtoList) {
                inventoryRepository.update(updateInventoryDto.getInventoryId(), updateInventoryDto.getOrderQuantity(), now, AppSecurityUtils.getCurrentAuditor());
            }
            return OrderPendingResponse.builder()
                    .orderId(request.getOrderId())
                    .orderStatus(OrderStatus.PROCESSING)
                    .accountNumber(request.getAccountNumber())
                    .itemList(processingItemList)
                    .build();
        }
        return OrderPendingResponse.builder()
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
                inventoryRepository.update(updateInventoryDto.getInventoryId(), updateInventoryDto.getOrderQuantity(), now, AppSecurityUtils.getCurrentAuditor());
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

