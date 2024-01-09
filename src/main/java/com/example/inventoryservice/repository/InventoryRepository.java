package com.example.inventoryservice.repository;

import com.example.inventoryservice.dto.InventoryDto;
import com.example.inventoryservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("""
            select new com.example.inventoryservice.dto.InventoryDto(p.id, p.price, i.id, i.quantity) 
            from Product p 
            inner join Inventory i on p.id = i.productId 
            where p.id in :productIdList
            """)
    List<InventoryDto> findAllByProductIdIn(List<Long> productIdList);


    @Modifying
    @Query("""
            update Inventory i 
            set i.quantity = (i.quantity - :orderQuantity), 
            i.lastModifiedDate = :now, 
            i.lastModifiedBy = :currentAuditor
            where i.id = :inventoryId
            """)
    void update(Long inventoryId, Integer orderQuantity, LocalDateTime now, String currentAuditor);
}
