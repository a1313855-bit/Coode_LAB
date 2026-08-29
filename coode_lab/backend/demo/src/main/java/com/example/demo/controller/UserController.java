package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.model.User;
import com.example.demo.service.UserService;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // =================
    // 會員註冊
    // =================
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody User user) {

        UserResponse createdUser = userService.createUser(user);

        return ResponseEntity.ok(createdUser);
    }

    // =================
    // 檢查Email是否已註冊
    // =================
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    // =================
    // 根據Email查詢會員
    // =================
    @GetMapping("/email")
    public ResponseEntity<UserResponse> findUserByEmail(@RequestParam String email) {
        return userService.findUserByEmail(email).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // =================
    // 根據 ID 查詢會員
    // =================
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> findUserById(@PathVariable Long userId) {
        Optional<UserResponse> user = userService.findUserById(userId);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // =================
    // 查詢所有會員
    // =================
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAllUsers() {
        List<UserResponse> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    // =================
    // 更新會員資料
    // =================
    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest request) {

        UserResponse updatedUser = userService.updateUser(userId, request);

        return ResponseEntity.ok(updatedUser);
    }

    // =================
    // 軟刪除(更新會員資料)
    // =================
    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(
            @PathVariable Long userId) {

        UserResponse deactivatedUser = userService.deactivateUser(userId);

        return ResponseEntity.ok(deactivatedUser);
    }

    // =================
    // 根據會員狀態查詢
    // =================
    @GetMapping("/status")
    public ResponseEntity<List<UserResponse>> findUsersByStatus(@RequestParam String status) {
        List<UserResponse> users = userService.findUsersByStatus(status);

        return ResponseEntity.ok(users);
    }

    // =================
    // 根據關鍵字查找會員
    // =================
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> findUserByName(@RequestParam String keyword) {
        List<UserResponse> users = userService.findUsersByName(keyword);

        return ResponseEntity.ok(users);
    }

    // =================
    // 會員變更密碼
    // =================
    @PatchMapping("/{userId}/password")
    public ResponseEntity<UserResponse> changePassword(@PathVariable Long userId, @RequestBody ChangePasswordRequest request) {
        UserResponse updatedUser = userService.changePassword(userId, request);

        return ResponseEntity.ok(updatedUser);
    }
}