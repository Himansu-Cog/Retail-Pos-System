package com.cognizant.retailpos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.cognizant.retailpos.entity.Promotion;
import com.cognizant.retailpos.enums.PromotionStatus;

public record PromotionDto(Long id, String code, String name, String description,
                           BigDecimal discountPercentage, LocalDateTime startDate,
                           LocalDateTime endDate, PromotionStatus status,
                           BigDecimal minimumPurchase, ProductDto product,
                           String applicableCategory) {
    public static PromotionDto from(Promotion promotion) {
        return new PromotionDto(promotion.getId(), promotion.getCode(), promotion.getName(),
                promotion.getDescription(), promotion.getDiscountPercentage(), promotion.getStartDate(),
                promotion.getEndDate(), promotion.getStatus(), promotion.getMinimumPurchase(),
                promotion.getProduct() == null ? null : ProductDto.from(promotion.getProduct()),
                promotion.getApplicableCategory());
    }
}
