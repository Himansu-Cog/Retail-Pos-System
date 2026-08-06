package com.cognizant.retailpos.entity;

import java.time.LocalDateTime;

import com.cognizant.retailpos.enums.StockUpdateReason;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "stock_log", indexes = {
        @Index(name = "idx_stock_log_product", columnList = "product_id"),
        @Index(name = "idx_stock_log_created", columnList = "createdAt")
})
public class StockLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_stocklog_product"))
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "performed_by", foreignKey = @ForeignKey(name = "fk_stocklog_user"))
    private User performedBy;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockUpdateReason updateReason;

    @Column(nullable = false)
    private int quantityChange;
    @Column(nullable = false)
    private int previousQuantity;
    @Column(nullable = false)
    private int newQuantity;

    @Size(max = 500)
    @Column(length = 500)
    private String remarks;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public User getPerformedBy() { return performedBy; }
    public void setPerformedBy(User performedBy) { this.performedBy = performedBy; }
    public StockUpdateReason getUpdateReason() { return updateReason; }
    public void setUpdateReason(StockUpdateReason updateReason) { this.updateReason = updateReason; }
    public int getQuantityChange() { return quantityChange; }
    public void setQuantityChange(int quantityChange) { this.quantityChange = quantityChange; }
    public int getPreviousQuantity() { return previousQuantity; }
    public void setPreviousQuantity(int previousQuantity) { this.previousQuantity = previousQuantity; }
    public int getNewQuantity() { return newQuantity; }
    public void setNewQuantity(int newQuantity) { this.newQuantity = newQuantity; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
