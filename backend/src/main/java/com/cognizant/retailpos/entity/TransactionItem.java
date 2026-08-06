package com.cognizant.retailpos.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "transaction_item", indexes = {
        @Index(name = "idx_transaction_item_transaction", columnList = "transaction_id"),
        @Index(name = "idx_transaction_item_product", columnList = "product_id")
})
public class TransactionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_transaction"))
    private SaleTransaction transaction;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_product"))
    private Product product;

    @Min(1)
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SaleTransaction getTransaction() { return transaction; }
    public void setTransaction(SaleTransaction transaction) { this.transaction = transaction; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
