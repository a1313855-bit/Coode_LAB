package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminPasswordRequest {
     @NotBlank(message = "新密碼不能為空")
     private String newPassword;
}
