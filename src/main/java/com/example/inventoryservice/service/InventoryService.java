package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.OrderPendingRequest;
import com.example.inventoryservice.dto.OrderPendingResponse;
import com.example.inventoryservice.integration.kafka.event.OrderPendingEvent;

public interface InventoryService {
    void handleOrderPendingEvent(OrderPendingEvent event);

    OrderPendingResponse handleOrderPendingOpenFeign(OrderPendingRequest request);
}
