package com.cognizant.retailpos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportDto(LocalDate startDate, LocalDate endDate, BigDecimal totalSales,
        long transactionCount, List<TransactionDto> transactions) {
}
