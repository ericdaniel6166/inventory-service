package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.OrderRequest;
import com.example.inventoryservice.dto.OrderResponse;
import com.example.inventoryservice.integration.kafka.event.OrderPendingEvent;

public interface InventoryService {
    void handleOrderPendingEvent(OrderPendingEvent event);

    OrderResponse handleOrderPendingOpenFeign(OrderRequest request);
}
