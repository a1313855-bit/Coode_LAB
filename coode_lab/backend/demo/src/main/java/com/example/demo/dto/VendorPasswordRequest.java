package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorPasswordRequest {

    @NotBlank(message = "目前密碼不能為空")
    private String currentPassword;

    @NotBlank(message = "新密碼不能為空")
    private String newPassword;
}
