package com.cognizant.retailpos.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDto(long totalProducts, long totalInventory, BigDecimal todaySales,
        BigDecimal weeklySales, BigDecimal monthlySales, long activePromotions,
        List<InventoryDto> lowStockProducts, List<TransactionDto> recentTransactions) {
}
