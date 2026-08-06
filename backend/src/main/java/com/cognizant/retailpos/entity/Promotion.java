package com.cognizant.retailpos.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cognizant.retailpos.enums.PromotionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "promotion", indexes = {
        @Index(name = "idx_promotion_code", columnList = "code"),
        @Index(name = "idx_promotion_dates", columnList = "startDate,endDate"),
        @Index(name = "idx_promotion_status", columnList = "status")
})
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 30)
    @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9_-]*$", message = "Promotion code contains invalid characters")
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9 .&'()-]*$", message = "Promotion name must start with a letter")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("100.00")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime startDate;
    @NotNull
    @Column(nullable = false)
    private LocalDateTime endDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PromotionStatus status;

    @DecimalMin("0.00")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumPurchase = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_promotion_product"))
    private Product product;

    @Size(max = 60)
    @Column(length = 60)
    private String applicableCategory;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public PromotionStatus getStatus() { return status; }
    public void setStatus(PromotionStatus status) { this.status = status; }
    public BigDecimal getMinimumPurchase() { return minimumPurchase; }
    public void setMinimumPurchase(BigDecimal minimumPurchase) { this.minimumPurchase = minimumPurchase; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getApplicableCategory() { return applicableCategory; }
    public void setApplicableCategory(String applicableCategory) { this.applicableCategory = applicableCategory; }
}
