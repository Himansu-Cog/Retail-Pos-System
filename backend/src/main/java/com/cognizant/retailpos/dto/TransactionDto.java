package com.cognizant.retailpos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.cognizant.retailpos.entity.SaleTransaction;
import com.cognizant.retailpos.entity.TransactionItem;
import com.cognizant.retailpos.enums.PaymentMode;
import com.cognizant.retailpos.enums.TransactionStatus;

public record TransactionDto(Long id, String receiptNumber, UserDto cashier,
        LocalDateTime transactionDate, BigDecimal subtotal, BigDecimal discountAmount,
        BigDecimal taxAmount, BigDecimal totalAmount, PaymentMode paymentMode,
        TransactionStatus status, List<TransactionItem> items) {
    public static TransactionDto from(SaleTransaction sale) {
        return new TransactionDto(sale.getId(), sale.getReceiptNumber(), UserDto.from(sale.getCashier()),
                sale.getTransactionDate(), sale.getSubtotal(), sale.getDiscountAmount(), sale.getTaxAmount(),
                sale.getTotalAmount(), sale.getPaymentMode(), sale.getStatus(), sale.getItems());
    }
}
