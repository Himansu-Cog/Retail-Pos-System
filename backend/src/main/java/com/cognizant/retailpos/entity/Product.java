package com.cognizant.retailpos.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cognizant.retailpos.enums.ProductCategory;
import com.cognizant.retailpos.enums.ProductStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "product", indexes = {
        @Index(name = "idx_product_barcode", columnList = "barcode"),
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_category", columnList = "category")
})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String barcode;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9 .&'()-]*$", message = "Product name must start with a letter")
    @Size(max = 120)
    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9 .&'()-]*$", message = "Supplier name must start with a letter")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String supplierMaster;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductCategory category;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus productStatus = ProductStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSupplierMaster() { return supplierMaster; }
    public void setSupplierMaster(String supplierMaster) { this.supplierMaster = supplierMaster; }
    public ProductCategory getCategory() { return category; }
    public void setCategory(ProductCategory category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public ProductStatus getProductStatus() { return productStatus; }
    public void setProductStatus(ProductStatus productStatus) { this.productStatus = productStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
