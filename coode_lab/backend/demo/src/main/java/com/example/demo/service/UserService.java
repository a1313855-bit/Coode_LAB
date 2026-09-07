package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.model.User;
import com.example.demo.util.SelectPartOfData;

public interface UserService {

    // ╔═══════════════════════╗
    // ║ 會員基本 CRUD 操作      ║
    // ╚═══════════════════════╝

    // ╔═══════════════════════════════════════╗
    // ║ 註冊/新增會員 CREATE                    ║
    // ╚═══════════════════════════════════════╝
    
    // 註冊、新增會員
    UserResponse createUser(User user);

    // ╔═══════════════════════════════════════╗
    // ║ 會員註冊/驗證 相關                       ║
    // ╚═══════════════════════════════════════╝
    boolean existsByEmail(String email);

    // 會員登入（驗證 Email、密碼與帳號狀態）
    UserResponse login(String email, String password);

    // ╔═══════════════════════════════════════╗
    // ║ 查詢會員 READ                          ║
    // ╚═══════════════════════════════════════╝

    // 查詢所有會員
    List<UserResponse> findAllUsers();

    // 查詢所有會員（分頁，固定每頁 10 筆，page 從 0 開始）
    SelectPartOfData.Result<UserResponse> findAllUsers(int page);

    // 根據ID查詢會員
    Optional<UserResponse> findUserById(Long userId);

    //  根據Email查詢會員
    Optional<UserResponse> findUserByEmail(String email);

    //  ╔═══════════════════════════════════════╗
    //  ║ 更新/修改會員資料   UPDATE               ║
    //  ╚═══════════════════════════════════════╝
    // 更新會員
    UserResponse updateUser(Long userId, UserUpdateRequest request);

    // 會員變更密碼
    UserResponse changePassword(Long userId, ChangePasswordRequest request);

    // 忘記密碼：依 Email 直接重設密碼（不需原密碼）
    UserResponse resetPassword(String email, String newPassword);

    // ╔═══════════════════════════════════════╗
    // ║ 軟刪除會員資料  DELETE                  ║
    // ╚═══════════════════════════════════════╝

    // (軟)刪除會員 / 啟用會員
    UserResponse toggleUserStatus(Long userId);

    // ╔═══════════════════════════════════════╗
    // ║ 管理員查詢用                            ║
    // ╚═══════════════════════════════════════╝

    // 依會員狀態查詢
    List<UserResponse> findUsersByStatus(String status);

    // 依關鍵字查詢會員(依姓名模糊搜尋)
    List<UserResponse> findUsersByName(String keyword);
}