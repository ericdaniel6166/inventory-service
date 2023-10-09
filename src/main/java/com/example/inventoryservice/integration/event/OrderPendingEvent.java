package com.example.inventoryservice.integration.event;

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
    List<OrderItemDto> orderItemList;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrderItemDto {
        Long productId;
        Integer quantity;
    }
}
