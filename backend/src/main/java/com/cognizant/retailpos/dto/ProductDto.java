package com.cognizant.retailpos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.cognizant.retailpos.entity.Product;
import com.cognizant.retailpos.enums.ProductCategory;
import com.cognizant.retailpos.enums.ProductStatus;

public record ProductDto(Long id, String barcode, String name, String supplierMaster,
                         ProductCategory category, BigDecimal price, ProductStatus productStatus,
                         BigDecimal taxPercentage, LocalDateTime createdAt) {
    public static ProductDto from(Product product) {
        return new ProductDto(product.getId(), product.getBarcode(), product.getName(),
                product.getSupplierMaster(), product.getCategory(), product.getPrice(),
                product.getProductStatus(), product.getCategory().getTaxPercentage(), product.getCreatedAt());
    }
}
