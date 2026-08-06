package com.cognizant.retailpos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.cognizant.retailpos.entity.StockLog;

public interface StockLogRepository extends JpaRepository<StockLog, Long> {
    @Query("select s from StockLog s order by s.createdAt desc")
    List<StockLog> findRecent(Pageable pageable);
    @Query("select s from StockLog s where s.product.id = :productId order by s.createdAt desc")
    List<StockLog> findForProduct(@Param("productId") Long productId);

    @Modifying
    @Query("delete from StockLog s where s.product.id = :productId")
    void deleteForProduct(@Param("productId") Long productId);
}
