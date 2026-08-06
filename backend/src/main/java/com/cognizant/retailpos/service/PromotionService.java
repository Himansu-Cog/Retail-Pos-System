package com.cognizant.retailpos.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

import com.cognizant.retailpos.entity.Promotion;
import com.cognizant.retailpos.enums.PromotionStatus;
import com.cognizant.retailpos.exception.BusinessException;
import com.cognizant.retailpos.exception.DuplicateResourceException;
import com.cognizant.retailpos.exception.ResourceNotFoundException;
import com.cognizant.retailpos.repository.PromotionRepository;

@Service
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final ProductService productService;

    public PromotionService(PromotionRepository promotionRepository, ProductService productService) {
        this.promotionRepository = promotionRepository;
        this.productService = productService;
    }

    public List<Promotion> findAll() { return promotionRepository.findAll(); }

    public Promotion findById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id " + id));
    }

    public List<Promotion> findActive() {
        LocalDateTime now = LocalDateTime.now();
        return promotionRepository.findActive(PromotionStatus.ACTIVE, now);
    }

    public Promotion create(Promotion promotion) {
        validate(promotion);
        if (promotionRepository.existsByCodeIgnoreCase(promotion.getCode())) {
            throw new DuplicateResourceException("Promotion code already exists");
        }
        promotion.setId(null);
        attachProduct(promotion);
        return promotionRepository.save(promotion);
    }

    public Promotion update(Long id, Promotion input) {
        validate(input);
        Promotion existing = findById(id);
        if (!existing.getCode().equalsIgnoreCase(input.getCode())
                && promotionRepository.existsByCodeIgnoreCase(input.getCode())) {
            throw new DuplicateResourceException("Promotion code already exists");
        }
        input.setId(id);
        attachProduct(input);
        return promotionRepository.save(input);
    }

    public void delete(Long id) { promotionRepository.delete(findById(id)); }

    private void validate(Promotion promotion) {
        if (!promotion.getEndDate().isAfter(promotion.getStartDate())) {
            throw new BusinessException("Promotion end date must be after start date");
        }
    }

    private void attachProduct(Promotion promotion) {
        if (promotion.getProduct() != null && promotion.getProduct().getId() != null) {
            promotion.setProduct(productService.findById(promotion.getProduct().getId()));
        } else {
            promotion.setProduct(null);
        }
    }
}
