package com.cognizant.retailpos.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cognizant.retailpos.entity.Inventory;
import com.cognizant.retailpos.entity.Product;
import com.cognizant.retailpos.entity.StockLog;
import com.cognizant.retailpos.entity.User;
import com.cognizant.retailpos.enums.StockUpdateReason;
import com.cognizant.retailpos.exception.BusinessException;
import com.cognizant.retailpos.exception.InsufficientStockException;
import com.cognizant.retailpos.exception.ResourceNotFoundException;
import com.cognizant.retailpos.repository.InventoryRepository;
import com.cognizant.retailpos.repository.StockLogRepository;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final StockLogRepository stockLogRepository;
    private final UserService userService;
    private final ProductService productService;

    public InventoryService(InventoryRepository inventoryRepository,
                            StockLogRepository stockLogRepository, UserService userService,
                            ProductService productService) {
        this.inventoryRepository = inventoryRepository;
        this.stockLogRepository = stockLogRepository;
        this.userService = userService;
        this.productService = productService;
    }

    public List<Inventory> findAll() { return inventoryRepository.findAll(); }
    public List<Inventory> findLowStock() { return inventoryRepository.findLowStockItems(); }
    public List<StockLog> findLogs() {
        return stockLogRepository.findRecent(org.springframework.data.domain.PageRequest.of(0, 100));
    }
    public List<StockLog> findLogsByProduct(Long productId) {
        return stockLogRepository.findForProduct(productId);
    }

    public Inventory findById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory record not found with id " + id));
    }

    public Inventory findByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product " + productId));
    }

    @Transactional
    public Inventory updateReorderLevel(Long id, int reorderLevel) {
        if (reorderLevel < 0) throw new BusinessException("Reorder level cannot be negative");
        Inventory inventory = findById(id);
        inventory.setReorderLevel(reorderLevel);
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory adjustStock(Long productId, int change, StockUpdateReason updateReason,
                                 String remarks, String username) {
        validateAudit(updateReason, remarks);
        Inventory inventory = findByProductId(productId);
        int previous = inventory.getQuantity();
        int updated = previous + change;
        if (updated < 0) {
            throw new InsufficientStockException(
                    "Insufficient stock for " + inventory.getProduct().getName());
        }
        inventory.setQuantity(updated);
        Inventory saved = inventoryRepository.save(inventory);

        StockLog log = new StockLog();
        log.setProduct(inventory.getProduct());
        User performedBy = username == null ? null : userService.findByUsername(username);
        log.setPerformedBy(performedBy);
        log.setUpdateReason(updateReason);
        log.setQuantityChange(change);
        log.setPreviousQuantity(previous);
        log.setNewQuantity(updated);
        log.setRemarks(remarks);
        stockLogRepository.save(log);
        return saved;
    }

    @Transactional
    public Inventory updateProductAndInventory(Long inventoryId, Product input, int quantity,
                                               int threshold, StockUpdateReason updateReason,
                                               String remarks, String username) {
        if (quantity < 0 || threshold < 0) {
            throw new BusinessException("Quantity and threshold cannot be negative");
        }
        validateAudit(updateReason, remarks);
        Inventory inventory = findById(inventoryId);
        int previous = inventory.getQuantity();
        Product updatedProduct = productService.updateFromInventory(inventory.getProduct().getId(), input);
        inventory.setProduct(updatedProduct);
        inventory.setQuantity(quantity);
        inventory.setReorderLevel(threshold);
        Inventory saved = inventoryRepository.save(inventory);

        StockLog log = new StockLog();
        log.setProduct(updatedProduct);
        log.setPerformedBy(username == null ? null : userService.findByUsername(username));
        log.setUpdateReason(updateReason == null ? StockUpdateReason.PRODUCT_DETAIL_UPDATE : updateReason);
        log.setQuantityChange(quantity - previous);
        log.setPreviousQuantity(previous);
        log.setNewQuantity(quantity);
        log.setRemarks(remarks);
        stockLogRepository.save(log);
        return saved;
    }

    private void validateAudit(StockUpdateReason updateReason, String remarks) {
        if (updateReason == null) throw new BusinessException("Stock update reason is required");
        if (remarks == null || remarks.isBlank()) {
            throw new BusinessException("Stock update remarks are required");
        }
        if (remarks.length() > 500) throw new BusinessException("Remarks cannot exceed 500 characters");
    }
}
