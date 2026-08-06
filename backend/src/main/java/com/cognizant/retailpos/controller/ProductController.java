package com.cognizant.retailpos.controller;

import java.security.Principal;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cognizant.retailpos.dto.ProductDto;
import com.cognizant.retailpos.entity.Product;
import com.cognizant.retailpos.service.ProductService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    public ProductController(ProductService productService) { this.productService = productService; }

    @GetMapping
    public Page<ProductDto> findAll(@RequestParam(defaultValue = "") String search,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        log.info("Listing products: page={}, size={}", page, size);
        return productService.findAll(search, page, size).map(ProductDto::from);
    }
    @GetMapping("/{id}")
    public ProductDto findById(@PathVariable Long id) {
        log.info("Loading product id={}", id);
        return ProductDto.from(productService.findById(id));
    }
    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody Product product,
                                          @RequestParam(defaultValue = "0") int quantity,
                                          @RequestParam(defaultValue = "10") int threshold,
                                          Principal principal) {
        log.info("Creating product name={}", product.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductDto.from(
                productService.create(product, quantity, threshold, principal.getName())));
    }
    @PutMapping("/{id}")
    public ProductDto update(@PathVariable Long id, @Valid @RequestBody Product product) {
        log.info("Updating product id={}", id);
        return ProductDto.from(productService.updateFromInventory(id, product));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deleting product id={}", id);
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
