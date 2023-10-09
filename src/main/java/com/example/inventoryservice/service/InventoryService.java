package com.example.inventoryservice.service;

import com.example.inventoryservice.integration.event.OrderPendingEvent;

public interface InventoryService {
    void handleOrderPendingEvent(OrderPendingEvent event);

}
