package com.cognizant.retailpos.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cognizant.retailpos.dto.ReportDto;
import com.cognizant.retailpos.dto.SettlementDto;
import com.cognizant.retailpos.entity.Settlement;
import com.cognizant.retailpos.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private static final Logger log = LoggerFactory.getLogger(ReportController.class);
    private final ReportService reportService;
    public ReportController(ReportService reportService) { this.reportService = reportService; }

    @GetMapping("/sales")
    public ReportDto sales(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        log.info("Generating sales report from {} to {}", startDate, endDate);
        return reportService.salesReport(startDate, endDate);
    }

    @GetMapping("/settlements")
    public List<SettlementDto> settlements() {
        log.info("Listing settlements");
        return reportService.findSettlements().stream().map(SettlementDto::from).toList();
    }

    @GetMapping("/settlements/{id}")
    public SettlementDto settlement(@PathVariable Long id) {
        log.info("Loading settlement id={}", id);
        return SettlementDto.from(reportService.findSettlement(id));
    }

    @PostMapping("/settlements")
    public ResponseEntity<SettlementDto> createSettlement(
            @RequestParam LocalDate date, @RequestParam Long cashierId,
            @RequestParam(defaultValue = "") String notes) {
        log.info("Creating settlement date={}, cashierId={}", date, cashierId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SettlementDto.from(reportService.createSettlement(date, cashierId, notes)));
    }

    @PutMapping("/settlements/{id}")
    public SettlementDto updateSettlement(@PathVariable Long id, @RequestBody Settlement settlement) {
        log.info("Updating settlement id={}", id);
        return SettlementDto.from(reportService.updateSettlement(id, settlement));
    }

    @DeleteMapping("/settlements/{id}")
    public ResponseEntity<Void> deleteSettlement(@PathVariable Long id) {
        log.info("Deleting settlement id={}", id);
        reportService.deleteSettlement(id);
        return ResponseEntity.noContent().build();
    }
}
