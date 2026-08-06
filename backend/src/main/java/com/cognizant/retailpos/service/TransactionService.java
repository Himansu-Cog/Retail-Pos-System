package com.cognizant.retailpos.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cognizant.retailpos.entity.Product;
import com.cognizant.retailpos.entity.Promotion;
import com.cognizant.retailpos.entity.SaleTransaction;
import com.cognizant.retailpos.entity.TransactionItem;
import com.cognizant.retailpos.enums.ProductStatus;
import com.cognizant.retailpos.enums.StockUpdateReason;
import com.cognizant.retailpos.enums.TransactionStatus;
import com.cognizant.retailpos.exception.BusinessException;
import com.cognizant.retailpos.exception.ResourceNotFoundException;
import com.cognizant.retailpos.repository.SaleTransactionRepository;

@Service
public class TransactionService {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final SaleTransactionRepository transactionRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final UserService userService;
    private final PromotionService promotionService;

    public TransactionService(SaleTransactionRepository transactionRepository, ProductService productService,
                              InventoryService inventoryService, UserService userService,
                              PromotionService promotionService) {
        this.transactionRepository = transactionRepository;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.userService = userService;
        this.promotionService = promotionService;
    }

    public List<SaleTransaction> findAll() { return transactionRepository.findAll(); }
    public List<SaleTransaction> findRecent() {
        return transactionRepository.findRecent(org.springframework.data.domain.PageRequest.of(0, 10));
    }

    public SaleTransaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id " + id));
    }

    @Transactional
    public SaleTransaction checkout(SaleTransaction request, Long promotionId, String username) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Add at least one product to the bill");
        }

        SaleTransaction sale = new SaleTransaction();
        sale.setReceiptNumber(createReceiptNumber());
        sale.setCashier(userService.findByUsername(username));
        sale.setPaymentMode(request.getPaymentMode());
        sale.setStatus(TransactionStatus.COMPLETED);
        sale.setTransactionDate(LocalDateTime.now());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        Promotion promotion = findPromotion(promotionId);

        for (TransactionItem requestedItem : request.getItems()) {
            Product product = productService.findById(requestedItem.getProduct().getId());
            if (product.getProductStatus() != ProductStatus.ACTIVE) {
                throw new BusinessException(product.getName() + " is not available for sale");
            }
            int quantity = requestedItem.getQuantity();
            if (quantity < 1) throw new BusinessException("Quantity must be at least 1");

            BigDecimal base = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            BigDecimal discount = calculateDiscount(product, base, promotion);
            BigDecimal taxable = base.subtract(discount);
            BigDecimal tax = taxable.multiply(product.getCategory().getTaxPercentage())
                    .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

            TransactionItem item = new TransactionItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            item.setDiscountAmount(discount);
            item.setTaxAmount(tax);
            item.setLineTotal(taxable.add(tax));
            sale.addItem(item);

            subtotal = subtotal.add(base);
            discountTotal = discountTotal.add(discount);
            taxTotal = taxTotal.add(tax);
        }

        if (promotionId != null && discountTotal.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("Selected promotion is not eligible for this basket");
        }

        for (TransactionItem item : sale.getItems()) {
            inventoryService.adjustStock(item.getProduct().getId(), -item.getQuantity(),
                    StockUpdateReason.SALE, "Sale " + sale.getReceiptNumber(), username);
        }
        sale.setSubtotal(subtotal);
        sale.setDiscountAmount(discountTotal);
        sale.setTaxAmount(taxTotal);
        sale.setTotalAmount(subtotal.subtract(discountTotal).add(taxTotal));
        return transactionRepository.save(sale);
    }

    @Transactional
    public SaleTransaction updateStatus(Long id, TransactionStatus status, String username) {
        SaleTransaction sale = findById(id);
        if (sale.getStatus() == TransactionStatus.COMPLETED
                && (status == TransactionStatus.CANCELLED || status == TransactionStatus.REFUNDED)) {
            for (TransactionItem item : sale.getItems()) {
                inventoryService.adjustStock(item.getProduct().getId(), item.getQuantity(),
                        StockUpdateReason.CUSTOMER_RETURN, status + " " + sale.getReceiptNumber(), username);
            }
        }
        sale.setStatus(status);
        return transactionRepository.save(sale);
    }

    public void delete(Long id) {
        SaleTransaction sale = findById(id);
        if (sale.getStatus() == TransactionStatus.COMPLETED) {
            throw new BusinessException("Complete transactions must be cancelled before deletion");
        }
        transactionRepository.delete(sale);
    }

    private Promotion findPromotion(Long promotionId) {
        if (promotionId == null) return null;
        return promotionService.findActive().stream()
                .filter(promotion -> promotion.getId().equals(promotionId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Selected promotion is no longer active"));
    }

    private BigDecimal calculateDiscount(Product product, BigDecimal base, Promotion promotion) {
        if (promotion == null || base.compareTo(promotion.getMinimumPurchase()) < 0) {
            return BigDecimal.ZERO;
        }
        boolean productMatches = promotion.getProduct() != null
                && promotion.getProduct().getId().equals(product.getId());
        boolean categoryMatches = promotion.getApplicableCategory() != null
                && promotion.getApplicableCategory().equalsIgnoreCase(product.getCategory().name());
        boolean storeWide = promotion.getProduct() == null
                && (promotion.getApplicableCategory() == null || promotion.getApplicableCategory().isBlank());
        if (!productMatches && !categoryMatches && !storeWide) return BigDecimal.ZERO;
        return base.multiply(promotion.getDiscountPercentage())
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    private String createReceiptNumber() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "R-" + time + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
