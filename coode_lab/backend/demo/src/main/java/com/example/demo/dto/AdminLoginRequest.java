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
public class AdminLoginRequest {
        //登入帳號
        @NotBlank(message = "Email 不能為空")
        @Email(message = "Email 格式不正確")
        private String email;
        //登入密碼
        @NotBlank(message = "密碼不能為空")
        private String password;
}
