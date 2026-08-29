package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.model.User;

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

    // ╔═══════════════════════════════════════╗
    // ║ 查詢會員 READ                          ║
    // ╚═══════════════════════════════════════╝

    // 查詢所有會員
    List<UserResponse> findAllUsers();

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

    // ╔═══════════════════════════════════════╗
    // ║ 軟刪除會員資料  DELETE                  ║
    // ╚═══════════════════════════════════════╝

    // (軟)刪除會員
    UserResponse deactivateUser(Long userId);

    // ╔═══════════════════════════════════════╗
    // ║ 管理員查詢用                            ║
    // ╚═══════════════════════════════════════╝

    // 依會員狀態查詢
    List<UserResponse> findUsersByStatus(String status);

    // 依關鍵字查詢會員(依姓名模糊搜尋)
    List<UserResponse> findUsersByName(String keyword);
}
