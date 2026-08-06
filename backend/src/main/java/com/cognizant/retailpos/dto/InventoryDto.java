package com.cognizant.retailpos.dto;

import java.time.LocalDateTime;
import com.cognizant.retailpos.entity.Inventory;

public record InventoryDto(Long id, ProductDto product, int quantity, int reorderLevel,
                           LocalDateTime updatedAt) {
    public static InventoryDto from(Inventory inventory) {
        return new InventoryDto(inventory.getId(), ProductDto.from(inventory.getProduct()),
                inventory.getQuantity(), inventory.getReorderLevel(), inventory.getUpdatedAt());
    }
}
