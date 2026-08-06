package com.cognizant.retailpos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.cognizant.retailpos.entity.Settlement;

public record SettlementDto(Long id, LocalDate settlementDate, UserDto cashier,
        int transactionCount, BigDecimal cashAmount, BigDecimal cardAmount, BigDecimal upiAmount,
        BigDecimal totalAmount, boolean closed, String notes, LocalDateTime createdAt) {
    public static SettlementDto from(Settlement settlement) {
        return new SettlementDto(settlement.getId(), settlement.getSettlementDate(),
                UserDto.from(settlement.getCashier()), settlement.getTransactionCount(),
                settlement.getCashAmount(), settlement.getCardAmount(), settlement.getUpiAmount(),
                settlement.getTotalAmount(), settlement.isClosed(), settlement.getNotes(), settlement.getCreatedAt());
    }
}
