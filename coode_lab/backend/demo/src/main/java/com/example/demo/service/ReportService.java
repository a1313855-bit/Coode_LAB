package com.example.demo.service;

// ========== Project ==========
import com.example.demo.dto.report.VendorDashboardDTO;
import com.example.demo.dto.report.VendorReportQuery;

public interface ReportService {

    // 廠商營運儀表板（一次回傳整頁資料）
    VendorDashboardDTO getVendorDashboard(Long vendorId, VendorReportQuery query);
}