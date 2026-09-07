package com.example.demo.controller;

// ========== Spring ==========
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// ========== Project ==========
import com.example.demo.dto.report.VendorDashboardDTO;
import com.example.demo.dto.report.VendorReportQuery;
import com.example.demo.service.ReportService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/reports")
public class ReportController {

    // ╔═══════════════╗
    // ║ Constructor   ║
    // ╚═══════════════╝
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // ╔══════════════════╗
    // ║ Dashboard API    ║
    // ╚══════════════════╝

    @GetMapping("/vendor/{vendorId}/dashboard")
    public ResponseEntity<VendorDashboardDTO> getVendorDashboard(
            @PathVariable Long vendorId,
            @Valid VendorReportQuery query) {
        return ResponseEntity.ok(reportService.getVendorDashboard(vendorId, query));
    }
}