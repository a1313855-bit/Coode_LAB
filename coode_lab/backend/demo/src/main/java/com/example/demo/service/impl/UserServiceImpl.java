package com.example.demo.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.model.Cart;
import com.example.demo.model.Order;
import com.example.demo.model.ReturnRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    // ╔═══════════════╗
    // ║ 依賴注入 ║
    // ╚═══════════════╝

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ╔═══════════════════════╗
    // ║ 會員基本 CRUD 操作 ║
    // ╚═══════════════════════╝

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public UserResponse createUser(User user) {

        // 1.檢查Email是否已被註冊
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("此 Email 已被註冊");
        }
        // 2.設定新會員預設狀態
        if (user.getStatus() == null || user.getStatus().isBlank()) {
            user.setStatus("ACTIVE");
        }
        // 設定密碼非明碼
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 3.建立該會員唯一的購物車
        Cart cart = new Cart();

        cart.setTotalQuantity(0);

        // 4.建立雙向關聯
        cart.setUser(user);
        user.setCart(cart);

        // 5.儲存 User
        // User -> Cart 有CascadeType.All,
        // 所以 Cart 也會一起被儲存
        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);

    }

    // 查詢所有會員
    @Override
    public List<UserResponse> findAllUsers() {
        return userRepository.findAll().stream().map(this::toUserResponse).toList();
    }

    // 用帳號( Email )查詢會員
    @Override
    public Optional<UserResponse> findUserByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toUserResponse);
    }

    // 用 ID 查詢會員
    @Override
    public Optional<UserResponse> findUserById(Long userId) {
        return userRepository.findById(userId).map(this::toUserResponse);
    }

    // UPDATE-更新/修改會員資料，選擇性更新(不可更新：帳號)
    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        // TODO Auto-generated method stub
        // 1.先確認會員是否存在
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("查無會員資料"));

        // 2.更新允許修改的欄位
        if (request.getName() != null) {
            existingUser.setName(request.getName());
        }
        if (request.getPhone() != null) {
            existingUser.setPhone(request.getPhone());
        }
        if (request.getCreditCard() != null) {
            existingUser.setCreditCard(request.getCreditCard());
        }
        if (request.getGender() != null) {
            existingUser.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            existingUser.setBirthday(request.getBirthday());
        }

        User savedUser = userRepository.save(existingUser);

        // 3.儲存更新後的會員
        return toUserResponse(savedUser);
    }

    // UPDATE-deactivateUser軟刪除 / 修改(更新UPDATE)會員狀態
    @Override
    @Transactional
    public UserResponse deactivateUser(Long userId) {
        // TODO Auto-generated method stub
        // 1.根據 userId 查詢會員
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("查無會員資料"));
        // 2.將會員狀態改為停用
        existingUser.setStatus("INACTIVE");

        User savedUser = userRepository.save(existingUser);

        // 3.儲存修改後的會員資料
        return toUserResponse(savedUser);
    }

    // READ-根據帳號狀態值查詢會員們
    @Override
    public List<UserResponse> findUsersByStatus(String status) {
        // TODO Auto-generated method stub
        return userRepository.findByStatus(status).stream().map(this::toUserResponse).toList();
    }

    // READ-依關鍵字查詢會員(依姓名模糊搜尋)
    @Override
    public List<UserResponse> findUsersByName(String keyword) {
        // TODO Auto-generated method stub
        return userRepository.findByNameContaining(keyword).stream().map(this::toUserResponse).toList();
    }

    // UPDATE-會員變更密碼
    @Override
    @Transactional
    public UserResponse changePassword(Long userId, ChangePasswordRequest request) {
        // TODO Auto-generated method stub
        User existingUser = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("查無會員資料"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), existingUser.getPassword())) {
            throw new IllegalArgumentException("目前密碼錯誤");
        }
        if (passwordEncoder.matches(request.getNewPassword(), existingUser.getPassword())) {
            throw new IllegalArgumentException("新密碼不可與目前密碼相同");
        }

        existingUser.setPassword(passwordEncoder.encode(request.getNewPassword()));

        User savedUser = userRepository.save(existingUser);

        return toUserResponse(savedUser);
    }

    // 回應前端
    private UserResponse toUserResponse(User user) {

        UserResponse response = new UserResponse();

        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setPhone(user.getPhone());
        response.setCreditCard(user.getCreditCard());
        response.setStatus(user.getStatus());
        response.setGender(user.getGender());
        response.setPicture(user.getPicture());
        response.setBirthday(user.getBirthday());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }
}