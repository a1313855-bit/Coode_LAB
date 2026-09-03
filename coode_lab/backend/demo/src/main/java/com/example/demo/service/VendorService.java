package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.VendorActivateRequest;
import com.example.demo.dto.VendorContractRequest;
import com.example.demo.dto.VendorLoginRequest;
import com.example.demo.dto.VendorPasswordRequest;
import com.example.demo.dto.VendorRequest;
import com.example.demo.dto.VendorResponse;
import com.example.demo.dto.VendorUpdateRequest;
import com.example.demo.util.SelectPartOfData;

public interface VendorService {

	// 查全部廠商,固定每頁10筆,page 從 0 開始
	SelectPartOfData.Result<VendorResponse> findAll(int page);

	// 可用廠商id查一個廠商,回傳VendorResponse
	VendorResponse findById(Long vendorId);

	// 管理員搜尋 / 篩選廠商 (固定每頁10筆,page 從 0 開始)
	SelectPartOfData.Result<VendorResponse> searchVendors(int page, String keyword, String status);

	// 廠商登入,帳號必須存在,密碼正確,狀態為ACTIVE,合約不能過期
	VendorResponse login(VendorLoginRequest request);

	// 管理員啟用廠商,狀態改為ACTIVE
	VendorResponse activateVendor(Long vendorId, VendorActivateRequest request);

	// 管理員停權廠商,狀態改為SUSPENDED
	VendorResponse suspendVendor(Long vendorId);

	// 管理員解除停權
	VendorResponse reactivateVendor(Long vendorId);

	// 管理員續約廠商
	VendorResponse renewContract(Long vendorId, VendorContractRequest request);

	// 新增廠商
	VendorResponse createVendor(VendorRequest request);

    // 修改廠商基本資料
    VendorResponse updateVendor(Long vendorId, VendorUpdateRequest request);

    // 廠商修改自己的密碼（需驗證目前密碼）
    VendorResponse changePassword(Long vendorId, VendorPasswordRequest request);

}
