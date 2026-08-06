package com.cognizant.retailpos.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.cognizant.retailpos.entity.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(Long productId);

    @Query("select i from Inventory i where i.quantity <= i.reorderLevel order by i.quantity")
    List<Inventory> findLowStockItems();

    @Query("select coalesce(sum(i.quantity), 0) from Inventory i")
    Long totalQuantity();
}
