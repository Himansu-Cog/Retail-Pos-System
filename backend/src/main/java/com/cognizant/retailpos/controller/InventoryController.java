package com.cognizant.retailpos.controller;

import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cognizant.retailpos.dto.InventoryDto;
import com.cognizant.retailpos.entity.Inventory;
import com.cognizant.retailpos.entity.Product;
import com.cognizant.retailpos.entity.StockLog;
import com.cognizant.retailpos.enums.StockUpdateReason;
import com.cognizant.retailpos.service.InventoryService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
    private final InventoryService inventoryService;
    public InventoryController(InventoryService inventoryService) { this.inventoryService = inventoryService; }

    @GetMapping
    public List<InventoryDto> findAll() {
        log.info("Listing inventory");
        return inventoryService.findAll().stream().map(InventoryDto::from).toList();
    }
    @GetMapping("/low-stock")
    public List<InventoryDto> lowStock() {
        log.info("Listing low-stock inventory");
        return inventoryService.findLowStock().stream().map(InventoryDto::from).toList();
    }
    @GetMapping("/logs")
    public List<StockLog> logs(@RequestParam(required = false) Long productId) {
        log.info("Listing stock logs for productId={}", productId);
        return productId == null ? inventoryService.findLogs() : inventoryService.findLogsByProduct(productId);
    }
    @PutMapping("/{id}/reorder-level")
    public InventoryDto updateReorderLevel(@PathVariable Long id, @RequestParam int value) {
        log.info("Updating reorder level for inventory id={}", id);
        return InventoryDto.from(inventoryService.updateReorderLevel(id, value));
    }
    @PostMapping("/adjust")
    public InventoryDto adjust(@RequestParam Long productId, @RequestParam int change,
                            @RequestParam StockUpdateReason reason,
                            @RequestParam(defaultValue = "") String remarks, Principal principal) {
        log.info("Adjusting stock for product id={}, change={}", productId, change);
        return InventoryDto.from(inventoryService.adjustStock(
                productId, change, reason, remarks, principal.getName()));
    }

    @PutMapping("/{id}")
    public InventoryDto updateProductAndInventory(@PathVariable Long id,
                                               @Valid @RequestBody Product product,
                                               @RequestParam int quantity,
                                               @RequestParam int threshold,
                                               @RequestParam StockUpdateReason reason,
                                               @RequestParam(defaultValue = "") String remarks,
                                               Principal principal) {
        log.info("Updating product and inventory id={}", id);
        return InventoryDto.from(inventoryService.updateProductAndInventory(
                id, product, quantity, threshold, reason, remarks, principal.getName()));
    }
}
