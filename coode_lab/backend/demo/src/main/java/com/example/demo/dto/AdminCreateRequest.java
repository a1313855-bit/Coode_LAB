package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

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
    @NotBlank(message = "Email 不能為空")
    @Email(message = "Email 格式不正確")
    private String email;
    //新增管理員時，前端傳入密碼
    @NotBlank(message = "密碼不能為空")
    private String password;
}
