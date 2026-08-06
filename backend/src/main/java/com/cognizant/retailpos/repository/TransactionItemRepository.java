package com.cognizant.retailpos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.cognizant.retailpos.entity.TransactionItem;

public interface TransactionItemRepository extends JpaRepository<TransactionItem, Long> {
    @Query("select count(i) from TransactionItem i where i.product.id = :productId")
    long countForProduct(@Param("productId") Long productId);
}
