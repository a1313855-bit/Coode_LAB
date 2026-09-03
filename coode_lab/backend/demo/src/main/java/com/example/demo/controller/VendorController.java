package com.example.demo.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.VendorActivateRequest;
import com.example.demo.dto.VendorContractRequest;
import com.example.demo.dto.VendorLoginRequest;
import com.example.demo.dto.VendorPasswordRequest;
import com.example.demo.dto.VendorRequest;
import com.example.demo.dto.VendorResponse;
import com.example.demo.dto.VendorUpdateRequest;
import com.example.demo.service.VendorService;
import com.example.demo.util.SelectPartOfData;

@RestController
@RequestMapping("/coode_lab/vendors")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    // 管理員查看全部廠商 (固定每頁10筆,page 從 0 開始)
    @GetMapping
    public SelectPartOfData.Result<VendorResponse> findAll(
            @RequestParam(defaultValue = "0") int page) {
        return vendorService.findAll(page);
    }

    // 管理員查看單一廠商資料詳細頁
    @GetMapping("/{vendorId}")
    public VendorResponse findById(@PathVariable Long vendorId) {
        return vendorService.findById(vendorId);
    }

    // 管理員搜尋 / 篩選廠商 (固定每頁10筆,page 從 0 開始)
    @GetMapping("/filter")
    public SelectPartOfData.Result<VendorResponse> searchVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {

        return vendorService.searchVendors(page, keyword, status);
    }

    // 廠商登入
    @PostMapping("/login")
    public VendorResponse login(@Valid @RequestBody VendorLoginRequest request) {
        return vendorService.login(request);
    }

    // 新增廠商
    @PostMapping
    public VendorResponse createVendor(@Valid @RequestBody VendorRequest request) {
        return vendorService.createVendor(request);
    }

    // 修改廠商
    @PutMapping("/{vendorId}")
    public VendorResponse updateVendor(@PathVariable Long vendorId, @Valid @RequestBody VendorUpdateRequest request) {
        return vendorService.updateVendor(vendorId, request);
    }

    // 廠商修改自己的密碼
    @PatchMapping("/{vendorId}/password")
    public VendorResponse changePassword(@PathVariable Long vendorId, @Valid @RequestBody VendorPasswordRequest request) {
        return vendorService.changePassword(vendorId, request);
    }

    // 管理員啟用廠商
    @PutMapping("/{vendorId}/activate")
    public VendorResponse activateVendor(@PathVariable Long vendorId, @Valid @RequestBody VendorActivateRequest request) {
        return vendorService.activateVendor(vendorId, request);
    }

    // 管理員停權廠商
    @PutMapping("/{vendorId}/suspend")
    public VendorResponse suspendVendor(@PathVariable Long vendorId) {
        return vendorService.suspendVendor(vendorId);
    }

    // 管理員解除停權
    @PutMapping("/{vendorId}/reactivate")
    public VendorResponse reactivateVendor(@PathVariable Long vendorId) {
        return vendorService.reactivateVendor(vendorId);
    }

    // 管理員續約廠商
    @PutMapping("/{vendorId}/renew-contract")
    public VendorResponse renewContract(@PathVariable Long vendorId, @Valid @RequestBody VendorContractRequest request) {
        return vendorService.renewContract(vendorId, request);
    }

}
