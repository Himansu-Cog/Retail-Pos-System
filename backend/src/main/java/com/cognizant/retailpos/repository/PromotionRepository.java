package com.cognizant.retailpos.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.cognizant.retailpos.entity.Promotion;
import com.cognizant.retailpos.enums.PromotionStatus;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    boolean existsByCodeIgnoreCase(String code);
    Optional<Promotion> findByCodeIgnoreCase(String code);
    @Query("select count(p) from Promotion p where p.status = :status and p.startDate <= :now and p.endDate >= :now")
    long countActive(@Param("status") PromotionStatus status, @Param("now") LocalDateTime now);

    @Query("select p from Promotion p where p.status = :status and p.startDate <= :now and p.endDate >= :now")
    List<Promotion> findActive(@Param("status") PromotionStatus status, @Param("now") LocalDateTime now);

    @Modifying
    @Query("update Promotion p set p.product = null where p.product.id = :productId")
    void detachProduct(@Param("productId") Long productId);
}
