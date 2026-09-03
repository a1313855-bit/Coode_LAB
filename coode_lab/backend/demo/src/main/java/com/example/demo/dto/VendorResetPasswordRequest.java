package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorResetPasswordRequest {

    @NotBlank(message = "新密碼不能為空")
    private String newPassword;
}
