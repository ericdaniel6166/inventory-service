package com.example.inventoryservice.api;

import com.example.inventoryservice.dto.OrderPendingRequest;
import com.example.inventoryservice.dto.OrderPendingResponse;
import com.example.inventoryservice.service.InventoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryApi {

    InventoryService inventoryService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        log.info("test");
        return ResponseEntity.ok("test");
    }

    @PostMapping("/handle-order-pending-open-feign")
    public ResponseEntity<OrderPendingResponse> handleOrderPendingOpenFeign(@RequestBody OrderPendingRequest request) {
        log.info("handleOrderPendingOpenFeign, orderId {}", request.getOrderId());
//        Thread.sleep(1000L * 60 * 60);
        return ResponseEntity.ok(inventoryService.handleOrderPendingOpenFeign(request));
    }

}


