package com.example.inventoryservice.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {

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
