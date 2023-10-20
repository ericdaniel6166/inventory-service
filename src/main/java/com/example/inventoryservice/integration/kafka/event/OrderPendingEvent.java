package com.example.inventoryservice.integration.kafka.event;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderPendingEvent {

    Long orderId;
    String accountNumber;
    List<Item> itemList;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Item {
        Long productId;
        Integer orderQuantity;
    }
}
