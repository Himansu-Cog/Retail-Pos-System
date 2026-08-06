package com.cognizant.retailpos.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.cognizant.retailpos.entity.SaleTransaction;
import com.cognizant.retailpos.enums.PaymentMode;
import com.cognizant.retailpos.enums.TransactionStatus;

public interface SaleTransactionRepository extends JpaRepository<SaleTransaction, Long> {
    @Query("select t from SaleTransaction t order by t.transactionDate desc")
    List<SaleTransaction> findRecent(Pageable pageable);
    @Query("select t from SaleTransaction t where t.transactionDate between :start and :end order by t.transactionDate desc")
    List<SaleTransaction> findForPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select count(t) from SaleTransaction t where t.cashier.id = :cashierId " +
            "and t.transactionDate between :start and :end and t.status = :status")
    long countCompleted(@Param("cashierId") Long cashierId, @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end, @Param("status") TransactionStatus status);

    @Query("select coalesce(sum(t.totalAmount), 0) from SaleTransaction t " +
            "where t.transactionDate between :start and :end and t.status = :status")
    BigDecimal sumSales(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                        @Param("status") TransactionStatus status);

    @Query("select coalesce(sum(t.totalAmount), 0) from SaleTransaction t " +
            "where t.cashier.id = :cashierId and t.transactionDate between :start and :end " +
            "and t.paymentMode = :mode and t.status = :status")
    BigDecimal sumByCashierAndMode(@Param("cashierId") Long cashierId,
                                   @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                   @Param("mode") PaymentMode mode, @Param("status") TransactionStatus status);
}
