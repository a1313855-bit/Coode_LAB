package com.example.demo.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.VendorActivateRequest;
import com.example.demo.dto.VendorContractRequest;
import com.example.demo.dto.VendorLoginRequest;
import com.example.demo.dto.VendorPasswordRequest;
import com.example.demo.dto.VendorRequest;
import com.example.demo.dto.VendorResponse;
import com.example.demo.dto.VendorUpdateRequest;
import com.example.demo.exception.VendorSpecification;
import com.example.demo.model.Vendor;
import com.example.demo.repository.VendorRepository;
import com.example.demo.service.VendorService;
import com.example.demo.util.SelectPartOfData;

@Service

public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;
    private final PasswordEncoder passwordEncoder;

    // 建構子注入
    // Spring Boot 會自動把 VendorRepository 放進來,把 Spring Boot 傳進來的
    // vendorRepository，存到這個左邊 Service 自己的 vendorRepository 裡。
    public VendorServiceImpl(VendorRepository vendorRepository, PasswordEncoder passwordEncoder) {
        this.vendorRepository = vendorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 查所有廠商 (固定每頁10筆)
    @Override
    public SelectPartOfData.Result<VendorResponse> findAll(int page) {

        List<VendorResponse> all = vendorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    // 根據vendorId查詢單一廠商
    @Override
    public VendorResponse findById(Long vendorId) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("找不到廠商 ID:" + vendorId));
        return toResponse(vendor);
    }

    // 管理員搜尋 / 篩選廠商 (固定每頁10筆)
    @Override
    public SelectPartOfData.Result<VendorResponse> searchVendors(int page, String keyword, String status) {

        Specification<Vendor> spec = VendorSpecification.keywordContains(keyword)
                .and(VendorSpecification.hasStatus(status));

        List<VendorResponse> all = vendorRepository.findAll(spec)
                .stream()
                .map(this::toResponse)
                .toList();
        return SelectPartOfData.pageOf10(all, page);
    }

    // 根據email查詢廠商並檢查密碼、帳號狀態、合約到期時間
    @Override
    public VendorResponse login(VendorLoginRequest request) {

        if (request.getEmail() == null
                || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email不能為空");
        }

        if (request.getPassword() == null
                || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("密碼不能為空");
        }

        // 根據email查詢廠商
        Vendor vendor = vendorRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("此廠商帳號不存在"));
        // 檢查密碼是否正確（資料庫可能是 BCrypt 加密或明文）
        String stored = vendor.getPassword();
        boolean passwordOk = stored != null
                && (passwordEncoder.matches(request.getPassword(), stored)
                    || stored.equals(request.getPassword()));
        if (!passwordOk) {
            throw new IllegalArgumentException("密碼錯誤");
        }

        // 檢查帳號是否啟用
        if (!"ACTIVE".equals(vendor.getStatus())) {
            throw new IllegalArgumentException("廠商帳號未啟用或已停權");
        }

        // 檢查合約是否到期
        if (vendor.getContractExpiresAt() == null) {
            throw new IllegalArgumentException("廠商尚未設定合約到期日");
        }

        if (!vendor.getContractExpiresAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("廠商合約已到期");
        }

        return toResponse(vendor);

    }

    // 管理員啟用廠商帳號
    @Override
    public VendorResponse activateVendor(Long vendorId, VendorActivateRequest request) {

        // 根據vendorId查詢廠商
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("找不到廠商 ID:" + vendorId));

        if (!"PENDING".equals(vendor.getStatus())) {
            throw new RuntimeException("只有待啟用的廠商可以進行首次啟用");
        }

        if (request.getActivatedAt() == null) {
            throw new RuntimeException("請輸入啟用日期");
        }

        if (request.getContractExpiresAt() == null) {
            throw new RuntimeException("請輸入合約到期日");
        }

        if (request.getContractExpiresAt()
                .isBefore(request.getActivatedAt())) {
            throw new RuntimeException("合約到期日不能早於啟用日期");
        }

        if (!request.getContractExpiresAt().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("合約到期日必須晚於現在時間");
        }

        // 把帳號狀態改為ACTIVE
        vendor.setStatus("ACTIVE");

        vendor.setActivatedAt(request.getActivatedAt());
        vendor.setContractExpiresAt(request.getContractExpiresAt());

        // 儲存更新後的廠商資訊,存回資料庫
        Vendor savedVendor = vendorRepository.save(vendor);

        return toResponse(savedVendor);

    }

    // 管理員停權廠商帳號
    @Override
    public VendorResponse suspendVendor(Long vendorId) {

        // 根據vendorId查詢廠商
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("找不到廠商 ID:" + vendorId));

        if ("PENDING".equals(vendor.getStatus())) {
            throw new RuntimeException("尚未啟用的廠商不能停權");
        }

        if ("SUSPENDED".equals(vendor.getStatus())) {
            throw new RuntimeException("此廠商已經是停權狀態");
        }

        // 把帳號狀態改為SUSPENDED
        vendor.setStatus("SUSPENDED");

        // 儲存更新後的廠商資訊,存回資料庫
        Vendor savedVendor = vendorRepository.save(vendor);

        return toResponse(savedVendor);
    }

    // 管理員解除停權
    @Override
    public VendorResponse reactivateVendor(Long vendorId) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("找不到廠商 ID:" + vendorId));

        if (!"SUSPENDED".equals(vendor.getStatus())) {
            throw new RuntimeException("此廠商目前不是停權狀態");
        }

        if (vendor.getContractExpiresAt() == null) {
            throw new RuntimeException("此廠商尚未設定合約到期日");
        }

        if (!vendor.getContractExpiresAt().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("廠商合約已到期，請先完成續約");
        }

        vendor.setStatus("ACTIVE");

        Vendor savedVendor = vendorRepository.save(vendor);

        return toResponse(savedVendor);
    }

    // 管理員續約廠商
    @Override
    public VendorResponse renewContract(Long vendorId, VendorContractRequest request) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("找不到廠商ID:" + vendorId));

        if ("PENDING".equals(vendor.getStatus())) {
            throw new RuntimeException("尚未啟用的廠商不能續約");
        }

        if (request.getContractExpiresAt() == null) {
            throw new RuntimeException("請輸入新的合約到期日");
        }

        if (!request.getContractExpiresAt().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("新的合約到期日必須晚於現在時間");
        }

        if (vendor.getContractExpiresAt() != null
                && !request.getContractExpiresAt()
                        .isAfter(vendor.getContractExpiresAt())) {
            throw new RuntimeException("新的合約到期日必須晚於原本的合約到期日");
        }

        vendor.setContractExpiresAt(request.getContractExpiresAt());

        Vendor savedVendor = vendorRepository.save(vendor);

        return toResponse(savedVendor);
    }

    // 修改廠商基本資料
    @Override
    public VendorResponse updateVendor(Long vendorId, VendorUpdateRequest request) {

        // 根據vendorId查詢廠商
        Vendor oldVendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("找不到廠商 ID:" + vendorId));

        if (request.getVendorName() == null
                || request.getVendorName().isBlank()) {
            throw new RuntimeException("廠商名稱不能為空");
        }

        if (request.getEmail() == null
                || request.getEmail().isBlank()) {
            throw new RuntimeException("Email不能為空");
        }

        // 檢查 Email 有沒有被「其他廠商」使用
        if (vendorRepository.existsByEmailAndVendorIdNot(
                request.getEmail(), vendorId)) {
            throw new RuntimeException("此 Email 已被其他廠商使用");
        }
        // 修改允許變更的欄位
        oldVendor.setVendorName(request.getVendorName());
        oldVendor.setEmail(request.getEmail());

        // 儲存更新後的廠商資訊,存回資料庫
        Vendor savedVendor = vendorRepository.save(oldVendor);

        return toResponse(savedVendor);
    }

    // 廠商修改自己的密碼
    @Override
    public VendorResponse changePassword(Long vendorId, VendorPasswordRequest request) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("找不到廠商 ID:" + vendorId));

        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            throw new RuntimeException("目前密碼不能為空");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new RuntimeException("新密碼不能為空");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), vendor.getPassword())) {
            throw new RuntimeException("目前密碼錯誤");
        }
        if (passwordEncoder.matches(request.getNewPassword(), vendor.getPassword())) {
            throw new RuntimeException("新密碼不可與目前密碼相同");
        }

        vendor.setPassword(passwordEncoder.encode(request.getNewPassword()));
        Vendor saved = vendorRepository.save(vendor);
        return toResponse(saved);
    }

    // 新增廠商
    @Override
    public VendorResponse createVendor(VendorRequest request) {
        if (request.getVendorName() == null
                || request.getVendorName().isBlank()) {
            throw new RuntimeException("廠商名稱不能為空");
        }

        if (request.getEmail() == null
                || request.getEmail().isBlank()) {
            throw new RuntimeException("Email不能為空");
        }

        if (request.getPassword() == null
                || request.getPassword().isBlank()) {
            throw new RuntimeException("密碼不能為空");
        }

        // 檢查email有沒有被使用過
        if (vendorRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("此Email已經註冊過");
        }

        // 建立一個新的 Vendor
        Vendor vendor = new Vendor();

        // 把管理員輸入的資料放進 Vendor
        vendor.setVendorName(request.getVendorName());
        vendor.setEmail(request.getEmail());
        vendor.setPassword(passwordEncoder.encode(request.getPassword()));

        // 新建立的廠商預設還沒啟用
        vendor.setStatus("PENDING");
        vendor.setActivatedAt(null);
        vendor.setContractExpiresAt(null);

        // 存進資料庫
        Vendor savedVendor = vendorRepository.save(vendor);

        // 轉成 VendorResponse 回傳，避免把密碼傳出去
        return toResponse(savedVendor);
    }

    // Vendor Entity 轉成 VendorResponse DTO
    private VendorResponse toResponse(Vendor vendor) {

        return new VendorResponse(
                vendor.getVendorId(),
                vendor.getVendorName(),
                vendor.getEmail(),
                vendor.getStatus(),
                vendor.getActivatedAt(),
                vendor.getContractExpiresAt(),
                vendor.getCreatedAt(),
                vendor.getUpdatedAt());
    }

}