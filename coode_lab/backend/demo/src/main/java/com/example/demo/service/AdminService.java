package com.example.demo.service;

import java.util.List;
import com.example.demo.model.Admin;

/**
 * AdminService
 */
public interface AdminService {

    //管理員登入
    Admin login(String email,String password);

    //新增管理員帳號
    Admin createAdmin(Admin admin);

    //查詢單一管理員
    Admin findById(Long adminId);

    //查詢全部管理員
    List<Admin> findAll();

    //修改管理員資料
    Admin updateAdmin(Admin admin);

    //修改管理員密碼 
    //當初設計這個功能的出發點是修改一般管理員資料、Email可以會不小心改到密碼
    void changePassword(Long adminId,String newPassword);

    //刪除管理員帳號                                                                                                                                                                                       
    void deleteAdmin(Long adminId);
}