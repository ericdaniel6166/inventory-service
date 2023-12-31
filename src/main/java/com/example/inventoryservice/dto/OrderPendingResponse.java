package com.example.inventoryservice.dto;

import com.example.inventoryservice.enums.OrderStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderPendingResponse {

    Long orderId;
    String accountNumber;
    OrderStatus orderStatus;
    List<Item> itemList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Builder
    public static class Item {
        Long productId;
        Integer orderQuantity;
        Integer inventoryQuantity;
        BigDecimal productPrice;
    }
}
