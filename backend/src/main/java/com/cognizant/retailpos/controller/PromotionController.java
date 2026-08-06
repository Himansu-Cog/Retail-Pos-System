package com.cognizant.retailpos.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cognizant.retailpos.dto.PromotionDto;
import com.cognizant.retailpos.entity.Promotion;
import com.cognizant.retailpos.service.PromotionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {
    private static final Logger log = LoggerFactory.getLogger(PromotionController.class);
    private final PromotionService promotionService;
    public PromotionController(PromotionService promotionService) { this.promotionService = promotionService; }

    @GetMapping
    public List<PromotionDto> findAll() {
        log.info("Listing promotions");
        return promotionService.findAll().stream().map(PromotionDto::from).toList();
    }
    @GetMapping("/active")
    public List<PromotionDto> findActive() {
        log.info("Listing active promotions");
        return promotionService.findActive().stream().map(PromotionDto::from).toList();
    }
    @GetMapping("/{id}")
    public PromotionDto findById(@PathVariable Long id) {
        log.info("Loading promotion id={}", id);
        return PromotionDto.from(promotionService.findById(id));
    }
    @PostMapping
    public ResponseEntity<PromotionDto> create(@Valid @RequestBody Promotion promotion) {
        log.info("Creating promotion code={}", promotion.getCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(PromotionDto.from(promotionService.create(promotion)));
    }
    @PutMapping("/{id}")
    public PromotionDto update(@PathVariable Long id, @Valid @RequestBody Promotion promotion) {
        log.info("Updating promotion id={}", id);
        return PromotionDto.from(promotionService.update(id, promotion));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deleting promotion id={}", id);
        promotionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
