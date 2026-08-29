package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateRequest {
    //新增管理員時，前端傳入的Email
    private String email;
    //新增管理員時，前端傳入密碼
    private String password;
}
