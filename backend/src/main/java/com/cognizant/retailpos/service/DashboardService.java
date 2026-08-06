package com.cognizant.retailpos.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import com.cognizant.retailpos.dto.DashboardDto;
import com.cognizant.retailpos.dto.InventoryDto;
import com.cognizant.retailpos.dto.TransactionDto;
import com.cognizant.retailpos.enums.PromotionStatus;
import com.cognizant.retailpos.enums.TransactionStatus;
import com.cognizant.retailpos.repository.InventoryRepository;
import com.cognizant.retailpos.repository.ProductRepository;
import com.cognizant.retailpos.repository.PromotionRepository;
import com.cognizant.retailpos.repository.SaleTransactionRepository;

@Service
public class DashboardService {
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final SaleTransactionRepository transactionRepository;
    private final PromotionRepository promotionRepository;

    public DashboardService(ProductRepository productRepository, InventoryRepository inventoryRepository,
                            SaleTransactionRepository transactionRepository, PromotionRepository promotionRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
        this.promotionRepository = promotionRepository;
    }

    public DashboardDto getSummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime weekStart = today.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        return new DashboardDto(productRepository.count(), inventoryRepository.totalQuantity(),
                transactionRepository.sumSales(todayStart, now, TransactionStatus.COMPLETED),
                transactionRepository.sumSales(weekStart, now, TransactionStatus.COMPLETED),
                transactionRepository.sumSales(monthStart, now, TransactionStatus.COMPLETED),
                promotionRepository.countActive(PromotionStatus.ACTIVE, now),
                inventoryRepository.findLowStockItems().stream().map(InventoryDto::from).toList(),
                transactionRepository.findRecent(org.springframework.data.domain.PageRequest.of(0, 10)).stream()
                        .map(TransactionDto::from).toList());
    }
}
