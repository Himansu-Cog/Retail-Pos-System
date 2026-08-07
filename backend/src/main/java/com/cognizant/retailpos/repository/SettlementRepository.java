package com.cognizant.retailpos.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.cognizant.retailpos.entity.Settlement;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    @Query("select s from Settlement s where s.settlementDate = :date and s.cashier.id = :cashierId")
    Optional<Settlement> findForCashierDate(@Param("date") LocalDate date,
                                            @Param("cashierId") Long cashierId);
}
