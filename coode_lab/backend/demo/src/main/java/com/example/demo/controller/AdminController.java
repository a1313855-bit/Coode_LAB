package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AdminCreateRequest;
import com.example.demo.dto.AdminLoginRequest;
import com.example.demo.dto.AdminPasswordRequest;
import com.example.demo.dto.AdminResponse;
import com.example.demo.model.Admin;
import com.example.demo.service.AdminService;


@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;


    // ==================== 建構子注入 ====================
    // Spring Boot 會自動將 AdminServiceImpl 注入到 AdminController
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }


    // ==================== 管理員登入 ====================
    // POST /api/admins/login
    @PostMapping("/login")
    public ResponseEntity<AdminResponse> login(
            @RequestBody AdminLoginRequest loginRequest) {

        /*
         * 1、取得前端傳入的 Email、Password
         * 2、呼叫 AdminService.login() 驗證登入
         */
        Admin admin = adminService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        /*
         * 3、將 Admin Entity 轉成 AdminResponse
         * 不將 password 回傳給前端
         */
        AdminResponse response = new AdminResponse(
                admin.getAdminId(),
                admin.getEmail()
        );

        /*
         * 4、登入成功回傳 200 OK
         */
        return ResponseEntity.ok(response);
    }


    // ==================== 新增管理員 ====================
    // POST /api/admins
    @PostMapping
    public ResponseEntity<AdminResponse> createAdmin(
            @RequestBody AdminCreateRequest request) {

        /*
         * 1、將前端傳入的 DTO 轉成 Admin Entity
         */
        Admin admin = new Admin();

        admin.setEmail(request.getEmail());
        admin.setPassword(request.getPassword());

        /*
         * 2、呼叫 Service 新增管理員
         */
        Admin createdAdmin = adminService.createAdmin(admin);

        /*
         * 3、轉成 AdminResponse
         * 不回傳 password
         */
        AdminResponse response = new AdminResponse(
                createdAdmin.getAdminId(),
                createdAdmin.getEmail()
        );

        /*
         * 4、新增成功回傳 201 Created
         */
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // ==================== 查詢單一管理員 ====================
    // GET /api/admins/{adminId}
    @GetMapping("/{adminId}")
    public ResponseEntity<AdminResponse> findById(
            @PathVariable("adminId") Long adminId) {

        /*
         * 1、呼叫 AdminService.findById()
         */
        Admin admin = adminService.findById(adminId);

        /*
         * 2、轉成 AdminResponse
         */
        AdminResponse response = new AdminResponse(
                admin.getAdminId(),
                admin.getEmail()
        );

        /*
         * 3、查詢成功回傳 200 OK
         */
        return ResponseEntity.ok(response);
    }


    // ==================== 查詢全部管理員 ====================
    // GET /api/admins
    @GetMapping
    public ResponseEntity<List<AdminResponse>> findAll() {

        /*
         * 1、查詢全部 Admin
         */
        List<Admin> admins = adminService.findAll();

        /*
         * 2、將 List<Admin> 轉成 List<AdminResponse>
         * 避免 password 回傳給前端
         */
        List<AdminResponse> responses = admins.stream()
                .map(admin -> new AdminResponse(
                        admin.getAdminId(),
                        admin.getEmail()
                ))
                .toList();

        /*
         * 3、查詢成功回傳 200 OK
         */
        return ResponseEntity.ok(responses);
    }


    // ==================== 修改管理員 ====================
    // PUT /api/admins/{adminId}
    @PutMapping("/{adminId}")
    public ResponseEntity<AdminResponse> updateAdmin(
            @PathVariable("adminId") Long adminId,
            @RequestBody AdminCreateRequest request) {

        /*
         * 1、建立 Admin 物件
         * adminId 告訴 Service 要修改哪位管理員
         *
         * AdminServiceImpl 目前只會修改 Email，
         * 不會修改 Password。
         */
        Admin admin = new Admin();

        admin.setAdminId(adminId);
        admin.setEmail(request.getEmail());

        /*
         * 2、呼叫 Service 修改管理員
         */
        Admin updatedAdmin = adminService.updateAdmin(admin);

        /*
         * 3、轉成 AdminResponse
         */
        AdminResponse response = new AdminResponse(
                updatedAdmin.getAdminId(),
                updatedAdmin.getEmail()
        );

        /*
         * 4、修改成功回傳 200 OK
         */
        return ResponseEntity.ok(response);
    }


    // ==================== 修改管理員密碼 ====================
    // PATCH /api/admins/{adminId}/password
    @PatchMapping("/{adminId}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable("adminId") Long adminId,
            @RequestBody AdminPasswordRequest request) {

        /*
         * 呼叫 AdminService.changePassword()
         */
        adminService.changePassword(
                adminId,
                request.getNewPassword()
        );

        /*
         * Service 的 changePassword() 回傳 void
         * 修改成功回傳 204 No Content
         */
        return ResponseEntity.noContent().build();
    }


    // ==================== 刪除管理員 ====================
    // DELETE /api/admins/{adminId}
    @DeleteMapping("/{adminId}")
    public ResponseEntity<Void> deleteAdmin(
            @PathVariable("adminId") Long adminId) {

        /*
         * 呼叫 AdminService.deleteAdmin()
         */
        adminService.deleteAdmin(adminId);

        /*
         * 刪除成功回傳 204 No Content
         */
        return ResponseEntity.noContent().build();
    }
}