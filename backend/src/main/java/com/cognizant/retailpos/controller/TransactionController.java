package com.cognizant.retailpos.controller;

import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cognizant.retailpos.dto.TransactionDto;
import com.cognizant.retailpos.entity.SaleTransaction;
import com.cognizant.retailpos.enums.TransactionStatus;
import com.cognizant.retailpos.service.TransactionService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;
    public TransactionController(TransactionService transactionService) { this.transactionService = transactionService; }

    @GetMapping
    public List<TransactionDto> findAll() {
        log.info("Listing transactions");
        return transactionService.findAll().stream().map(TransactionDto::from).toList();
    }
    @GetMapping("/{id}")
    public TransactionDto findById(@PathVariable Long id) {
        log.info("Loading transaction id={}", id);
        return TransactionDto.from(transactionService.findById(id));
    }
    @PostMapping
    public ResponseEntity<TransactionDto> checkout(@Valid @RequestBody SaleTransaction transaction,
                                                     @RequestParam(required = false) Long promotionId,
                                                     Principal principal) {
        log.info("Processing checkout for username={}", principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionDto.from(
                transactionService.checkout(transaction, promotionId, principal.getName())));
    }
    @PutMapping("/{id}/status")
    public TransactionDto updateStatus(@PathVariable Long id, @RequestParam TransactionStatus status,
                                        Principal principal) {
        log.info("Updating transaction id={} to status={}", id, status);
        return TransactionDto.from(transactionService.updateStatus(id, status, principal.getName()));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deleting transaction id={}", id);
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
