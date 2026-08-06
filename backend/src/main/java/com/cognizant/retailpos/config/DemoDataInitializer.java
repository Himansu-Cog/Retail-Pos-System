package com.cognizant.retailpos.config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cognizant.retailpos.entity.*;
import com.cognizant.retailpos.enums.*;
import com.cognizant.retailpos.repository.*;

@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StockLogRepository stockLogRepository;
    private final PromotionRepository promotionRepository;
    private final SaleTransactionRepository transactionRepository;
    private final SettlementRepository settlementRepository;
    private final PasswordEncoder passwordEncoder;
    private final String demoPassword;

    public DemoDataInitializer(UserRepository userRepository, ProductRepository productRepository,
                               InventoryRepository inventoryRepository, StockLogRepository stockLogRepository,
                               PromotionRepository promotionRepository,
                               SaleTransactionRepository transactionRepository,
                               SettlementRepository settlementRepository, PasswordEncoder passwordEncoder,
                               @Value("${app.demo-password}") String demoPassword) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockLogRepository = stockLogRepository;
        this.promotionRepository = promotionRepository;
        this.transactionRepository = transactionRepository;
        this.settlementRepository = settlementRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        createUsersWhenEmpty();
        createCatalogWhenEmpty();
        createPromotionsWhenEmpty();
        createTransactionsWhenEmpty();
        createSettlementWhenEmpty();
    }

    private void createUsersWhenEmpty() {
        if (userRepository.count() > 0) return;
        userRepository.saveAll(List.of(
                user("admin", "System Admin", "admin@retailflow.local", UserRole.ADMIN),
                user("manager", "Store Manager", "manager@retailflow.local", UserRole.STORE_MANAGER),
                user("cashier", "Store Cashier", "cashier@retailflow.local", UserRole.CASHIER),
                user("inventory", "Inventory Associate", "inventory@retailflow.local", UserRole.INVENTORY_ASSOCIATE)
        ));
    }

    private User user(String username, String fullName, String email, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(demoPassword));
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private void createCatalogWhenEmpty() {
        if (productRepository.count() > 0) return;
        List<Product> products = productRepository.saveAll(List.of(
                product("India Gate Basmati Rice", ProductCategory.GROCERY, "India Gate Foods", "699.00"),
                product("Tropicana Orange Juice", ProductCategory.BEVERAGES, "PepsiCo India", "125.00"),
                product("Dettol Liquid Hand Wash", ProductCategory.HOME_CARE, "Reckitt India", "149.00"),
                product("Happilo Roasted Almonds", ProductCategory.SNACKS, "Happilo Foods", "299.00"),
                product("Himalaya Herbal Shampoo", ProductCategory.PERSONAL_CARE, "Himalaya Wellness", "225.00"),
                product("Amul Taaza Milk", ProductCategory.DAIRY, "Amul Dairy", "72.00"),
                product("Britannia Whole Wheat Bread", ProductCategory.BAKERY, "Britannia Industries", "55.00"),
                product("Surf Excel Matic", ProductCategory.HOME_CARE, "Hindustan Unilever", "435.00"),
                product("Nescafe Classic Coffee", ProductCategory.BEVERAGES, "Nestle India", "345.00"),
                product("Lays Classic Salted", ProductCategory.SNACKS, "PepsiCo India", "50.00"),
                product("Dove Bathing Bar Pack", ProductCategory.PERSONAL_CARE, "Hindustan Unilever", "210.00"),
                product("Aashirvaad Whole Wheat Atta", ProductCategory.GROCERY, "ITC Foods", "310.00")
        ));
        int[] quantities = {46, 8, 31, 19, 7, 52, 11, 24, 15, 63, 9, 37};
        int[] reorderLevels = {12, 12, 10, 8, 10, 18, 12, 8, 10, 20, 10, 12};
        User associate = userRepository.findByUsername("inventory").orElse(null);
        for (int index = 0; index < products.size(); index++) {
            Inventory inventory = new Inventory();
            inventory.setProduct(products.get(index));
            inventory.setQuantity(quantities[index]);
            inventory.setReorderLevel(reorderLevels[index]);
            inventoryRepository.save(inventory);

            StockLog log = new StockLog();
            log.setProduct(products.get(index));
            log.setPerformedBy(associate);
            log.setUpdateReason(StockUpdateReason.NEW_ITEM);
            log.setQuantityChange(quantities[index]);
            log.setPreviousQuantity(0);
            log.setNewQuantity(quantities[index]);
            log.setRemarks("Opening stock for demo store");
            stockLogRepository.save(log);
        }
    }

    private Product product(String name, ProductCategory category, String supplier, String price) {
        Product product = new Product();
        product.setBarcode("890" + String.format("%010d", Math.abs((long) name.hashCode())));
        product.setName(name);
        product.setSupplierMaster(supplier);
        product.setCategory(category);
        product.setPrice(new BigDecimal(price));
        product.setProductStatus(ProductStatus.ACTIVE);
        return product;
    }

    private void createPromotionsWhenEmpty() {
        if (promotionRepository.count() > 0) return;
        LocalDateTime now = LocalDateTime.now();
        List<Product> products = productRepository.findAll();
        Promotion storeWide = promotion("WELCOME5", "Welcome Savings", "5.00",
                now.minusDays(10), now.plusMonths(3), PromotionStatus.ACTIVE);
        storeWide.setMinimumPurchase(new BigDecimal("500.00"));

        Promotion snacks = promotion("SNACK10", "Snack Time Deal", "10.00",
                now.minusDays(2), now.plusDays(28), PromotionStatus.ACTIVE);
        snacks.setApplicableCategory(ProductCategory.SNACKS.name());
        snacks.setMinimumPurchase(new BigDecimal("150.00"));

        Promotion coffee = promotion("COFFEE15", "Coffee Festival", "15.00",
                now.plusDays(5), now.plusDays(20), PromotionStatus.SCHEDULED);
        products.stream().filter(item -> item.getName().equals("Nescafe Classic Coffee")).findFirst().ifPresent(coffee::setProduct);
        coffee.setMinimumPurchase(BigDecimal.ZERO);
        promotionRepository.saveAll(List.of(storeWide, snacks, coffee));
    }

    private Promotion promotion(String code, String name, String percentage, LocalDateTime start,
                                LocalDateTime end, PromotionStatus status) {
        Promotion promotion = new Promotion();
        promotion.setCode(code);
        promotion.setName(name);
        promotion.setDescription(name + " for Retail System customers");
        promotion.setDiscountPercentage(new BigDecimal(percentage));
        promotion.setStartDate(start);
        promotion.setEndDate(end);
        promotion.setStatus(status);
        promotion.setMinimumPurchase(BigDecimal.ZERO);
        return promotion;
    }

    private void createTransactionsWhenEmpty() {
        if (transactionRepository.count() > 0 || productRepository.count() == 0) return;
        User cashier = userRepository.findByUsername("cashier").orElseThrow();
        List<Product> products = productRepository.findAll();
        List<SaleTransaction> sales = new ArrayList<>();
        sales.add(sale("R-DEMO-1001", cashier, LocalDateTime.now().minusHours(2),
                PaymentMode.UPI, List.of(products.get(0), products.get(6)), List.of(1, 2)));
        sales.add(sale("R-DEMO-1002", cashier, LocalDateTime.now().minusHours(5),
                PaymentMode.CARD, List.of(products.get(1), products.get(3)), List.of(2, 1)));
        sales.add(sale("R-DEMO-1003", cashier, LocalDateTime.now().minusDays(1).withHour(18),
                PaymentMode.CASH, List.of(products.get(2), products.get(9)), List.of(1, 3)));
        sales.add(sale("R-DEMO-1004", cashier, LocalDateTime.now().minusDays(2).withHour(14),
                PaymentMode.UPI, List.of(products.get(5), products.get(11)), List.of(3, 1)));
        sales.add(sale("R-DEMO-1005", cashier, LocalDateTime.now().minusDays(5).withHour(11),
                PaymentMode.CARD, List.of(products.get(7), products.get(10)), List.of(1, 1)));
        transactionRepository.saveAll(sales);
    }

    private SaleTransaction sale(String receipt, User cashier, LocalDateTime date, PaymentMode mode,
                                 List<Product> products, List<Integer> quantities) {
        SaleTransaction sale = new SaleTransaction();
        sale.setReceiptNumber(receipt);
        sale.setCashier(cashier);
        sale.setTransactionDate(date);
        sale.setPaymentMode(mode);
        sale.setStatus(TransactionStatus.COMPLETED);
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        for (int index = 0; index < products.size(); index++) {
            Product product = products.get(index);
            int quantity = quantities.get(index);
            BigDecimal base = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            BigDecimal tax = base.multiply(product.getCategory().getTaxPercentage())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            TransactionItem item = new TransactionItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            item.setDiscountAmount(BigDecimal.ZERO);
            item.setTaxAmount(tax);
            item.setLineTotal(base.add(tax));
            sale.addItem(item);
            subtotal = subtotal.add(base);
            taxTotal = taxTotal.add(tax);
        }
        sale.setSubtotal(subtotal);
        sale.setDiscountAmount(BigDecimal.ZERO);
        sale.setTaxAmount(taxTotal);
        sale.setTotalAmount(subtotal.add(taxTotal));
        return sale;
    }

    private void createSettlementWhenEmpty() {
        if (settlementRepository.count() > 0 || transactionRepository.count() == 0) return;
        User cashier = userRepository.findByUsername("cashier").orElseThrow();
        LocalDate date = LocalDate.now().minusDays(1);
        BigDecimal cash = transactionRepository.sumByCashierAndMode(
                cashier.getId(), date.atStartOfDay(), date.plusDays(1).atStartOfDay().minusNanos(1),
                PaymentMode.CASH, TransactionStatus.COMPLETED);
        Settlement settlement = new Settlement();
        settlement.setSettlementDate(date);
        settlement.setCashier(cashier);
        settlement.setTransactionCount(1);
        settlement.setCashAmount(cash);
        settlement.setCardAmount(BigDecimal.ZERO);
        settlement.setUpiAmount(BigDecimal.ZERO);
        settlement.setTotalAmount(cash);
        settlement.setClosed(true);
        settlement.setNotes("Demo end-of-day settlement");
        settlementRepository.save(settlement);
    }
}
