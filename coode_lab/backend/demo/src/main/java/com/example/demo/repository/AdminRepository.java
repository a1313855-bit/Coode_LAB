package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Admin;

public interface AdminRepository extends JpaRepository<Admin,Long>{ 

    //用email查詢
    /*
    查一筆，而且可能不存在，回傳 Optional<Admin>
    */
    Optional<Admin> findByEmail(String email);

    //判斷Email是否已經存在
    boolean existsByEmail(String email);
}
