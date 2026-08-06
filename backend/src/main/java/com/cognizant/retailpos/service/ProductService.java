package com.cognizant.retailpos.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cognizant.retailpos.entity.Inventory;
import com.cognizant.retailpos.entity.Product;
import com.cognizant.retailpos.entity.StockLog;
import com.cognizant.retailpos.enums.ProductStatus;
import com.cognizant.retailpos.enums.StockUpdateReason;
import com.cognizant.retailpos.exception.BusinessException;
import com.cognizant.retailpos.exception.DuplicateResourceException;
import com.cognizant.retailpos.exception.ResourceNotFoundException;
import com.cognizant.retailpos.repository.InventoryRepository;
import com.cognizant.retailpos.repository.ProductRepository;
import com.cognizant.retailpos.repository.PromotionRepository;
import com.cognizant.retailpos.repository.StockLogRepository;
import com.cognizant.retailpos.repository.TransactionItemRepository;
import com.cognizant.retailpos.repository.UserRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StockLogRepository stockLogRepository;
    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final TransactionItemRepository transactionItemRepository;

    public ProductService(ProductRepository productRepository, InventoryRepository inventoryRepository,
                          StockLogRepository stockLogRepository, UserRepository userRepository,
                          PromotionRepository promotionRepository,
                          TransactionItemRepository transactionItemRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockLogRepository = stockLogRepository;
        this.userRepository = userRepository;
        this.promotionRepository = promotionRepository;
        this.transactionItemRepository = transactionItemRepository;
    }

    public Page<Product> findAll(String search, int page, int size) {
        PageRequest request = PageRequest.of(page, size, Sort.by("name").ascending());
        if (search == null || search.isBlank()) return productRepository.findAll(request);
        String value = search.trim();
        return productRepository.search(value, request);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    @Transactional
    public Product create(Product product, int initialQuantity, int threshold, String username) {
        if (initialQuantity < 0 || threshold < 0) {
            throw new BusinessException("Quantity and threshold cannot be negative");
        }
        ensureUniqueName(product.getName(), null);
        product.setId(null);
        product.setBarcode(generateBarcode());
        if (product.getProductStatus() == null) product.setProductStatus(ProductStatus.ACTIVE);
        Product saved = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(saved);
        inventory.setQuantity(initialQuantity);
        inventory.setReorderLevel(threshold);
        inventoryRepository.save(inventory);

        StockLog log = new StockLog();
        log.setProduct(saved);
        log.setPerformedBy(userRepository.findByUsername(username).orElse(null));
        log.setUpdateReason(StockUpdateReason.NEW_ITEM);
        log.setQuantityChange(initialQuantity);
        log.setPreviousQuantity(0);
        log.setNewQuantity(initialQuantity);
        log.setRemarks("New product created");
        stockLogRepository.save(log);
        return saved;
    }

    @Transactional
    public Product updateFromInventory(Long id, Product input) {
        Product existing = findById(id);
        ensureUniqueName(input.getName(), id);
        existing.setName(input.getName());
        existing.setSupplierMaster(input.getSupplierMaster());
        existing.setCategory(input.getCategory());
        existing.setPrice(input.getPrice());
        existing.setProductStatus(input.getProductStatus());
        return productRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        if (transactionItemRepository.countForProduct(id) > 0) {
            throw new BusinessException("Product used in a completed sale cannot be deleted; mark it inactive instead");
        }
        promotionRepository.detachProduct(id);
        stockLogRepository.deleteForProduct(id);
        inventoryRepository.findByProductId(id).ifPresent(inventoryRepository::delete);
        productRepository.delete(product);
    }

    private void ensureUniqueName(String name, Long id) {
        long matches = id == null ? productRepository.countName(name)
                : productRepository.countNameExcept(name, id);
        if (matches > 0) {
            throw new DuplicateResourceException(
                    "Product name already exists. Add a variant such as 100gm or 200gm");
        }
    }

    private String generateBarcode() {
        String barcode;
        do {
            long number = Math.abs(System.nanoTime() % 10_000_000_000L);
            barcode = "890" + String.format("%010d", number);
        } while (productRepository.existsByBarcode(barcode));
        return barcode;
    }
}
