package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
    //1.根據Email找尋會員
    Optional<User> findByEmail(String email);

    //2.判斷Email是否已經被註冊
    boolean existsByEmail(String email);

    //3.依會員狀態查詢<--供管理員查詢用（屬性名status）
    List<User> findByStatus(String status);

    //4.依關鍵字查詢會員(依姓名模糊搜尋)<--供管理員查詢
    List<User> findByNameContaining(String keyword);
}
