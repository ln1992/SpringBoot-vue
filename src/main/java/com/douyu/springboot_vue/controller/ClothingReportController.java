package com.douyu.springboot_vue.controller;

import com.douyu.springboot_vue.entities.Clothing;
import com.douyu.springboot_vue.service.ClothingReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/clothing-reports")
@CrossOrigin(origins = "*")
public class ClothingReportController {

    private static final Logger logger = Logger.getLogger(ClothingReportController.class.getName());

    @Autowired
    private ClothingReportService clothingReportService;

    // 库存汇总报表
    @GetMapping("/inventory-summary")
    public ResponseEntity<Map<String, Object>> getInventorySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate) {
        try {
            Map<String, Object> result = clothingReportService.getInventorySummaryReport(startDate, endDate);
            logger.info("Successfully generated inventory summary report");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.severe("Error generating inventory summary report: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // 低库存报表
    @GetMapping("/low-stock")
    public ResponseEntity<List<Clothing>> getLowStockReport() {
        try {
            List<Clothing> lowStockItems = clothingReportService.getLowStockReport();
            logger.info("Successfully retrieved low stock report with " + lowStockItems.size() + " items");
            return ResponseEntity.ok(lowStockItems);
        } catch (Exception e) {
            logger.severe("Error retrieving low stock report: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    // 出入库统计报表
    @GetMapping("/inbound-outbound")
    public ResponseEntity<Map<String, Object>> getInboundOutboundReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate) {
        try {
            Map<String, Object> result = clothingReportService.getInboundOutboundReport(startDate, endDate);
            logger.info("Successfully generated inbound-outbound report");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.severe("Error generating inbound-outbound report: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}