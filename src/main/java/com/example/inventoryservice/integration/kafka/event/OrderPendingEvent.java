package com.example.inventoryservice.integration.kafka.event;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderPendingEvent implements Serializable {
    static final long serialVersionUID = 12346L;

    Long orderId;
    List<OrderPendingItem> orderPendingItemList;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrderPendingItem {
        Long productId;
        Integer orderQuantity;
    }
}
