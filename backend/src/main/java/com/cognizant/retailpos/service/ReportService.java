package com.cognizant.retailpos.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import com.cognizant.retailpos.dto.ReportDto;
import com.cognizant.retailpos.dto.TransactionDto;
import com.cognizant.retailpos.entity.Settlement;
import com.cognizant.retailpos.entity.User;
import com.cognizant.retailpos.enums.PaymentMode;
import com.cognizant.retailpos.enums.TransactionStatus;
import com.cognizant.retailpos.exception.DuplicateResourceException;
import com.cognizant.retailpos.exception.BusinessException;
import com.cognizant.retailpos.exception.ResourceNotFoundException;
import com.cognizant.retailpos.repository.SaleTransactionRepository;
import com.cognizant.retailpos.repository.SettlementRepository;

@Service
public class ReportService {
    private final SaleTransactionRepository transactionRepository;
    private final SettlementRepository settlementRepository;
    private final UserService userService;

    public ReportService(SaleTransactionRepository transactionRepository,
                         SettlementRepository settlementRepository, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.settlementRepository = settlementRepository;
        this.userService = userService;
    }

    public ReportDto salesReport(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("Report end date cannot be before start date");
        }
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        var transactions = transactionRepository.findForPeriod(start, end);
        BigDecimal sales = transactionRepository.sumSales(start, end, TransactionStatus.COMPLETED);
        long count = transactions.stream().filter(t -> t.getStatus() == TransactionStatus.COMPLETED).count();
        return new ReportDto(startDate, endDate, sales, count,
                transactions.stream().map(TransactionDto::from).toList());
    }

    public List<Settlement> findSettlements() { return settlementRepository.findAll(); }

    public Settlement findSettlement(Long id) {
        return settlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found with id " + id));
    }

    public Settlement createSettlement(LocalDate date, Long cashierId, String notes) {
        if (settlementRepository.findForCashierDate(date, cashierId).isPresent()) {
            throw new DuplicateResourceException("Settlement already exists for this cashier and date");
        }
        User cashier = userService.findById(cashierId);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);
        BigDecimal cash = sum(cashierId, start, end, PaymentMode.CASH);
        BigDecimal card = sum(cashierId, start, end, PaymentMode.CARD);
        BigDecimal upi = sum(cashierId, start, end, PaymentMode.UPI);

        Settlement settlement = new Settlement();
        settlement.setSettlementDate(date);
        settlement.setCashier(cashier);
        settlement.setTransactionCount((int) transactionRepository
                .countCompleted(
                        cashierId, start, end, TransactionStatus.COMPLETED));
        settlement.setCashAmount(cash);
        settlement.setCardAmount(card);
        settlement.setUpiAmount(upi);
        settlement.setTotalAmount(cash.add(card).add(upi));
        settlement.setClosed(true);
        settlement.setNotes(notes);
        return settlementRepository.save(settlement);
    }

    public Settlement updateSettlement(Long id, Settlement input) {
        Settlement existing = findSettlement(id);
        existing.setNotes(input.getNotes());
        existing.setClosed(input.isClosed());
        return settlementRepository.save(existing);
    }

    public void deleteSettlement(Long id) {
        settlementRepository.delete(findSettlement(id));
    }

    private BigDecimal sum(Long cashierId, LocalDateTime start, LocalDateTime end, PaymentMode mode) {
        return transactionRepository.sumByCashierAndMode(
                cashierId, start, end, mode, TransactionStatus.COMPLETED);
    }
}
