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
public class VendorUpdateRequest {

    @NotBlank(message = "廠商名稱不能為空")
    private String vendorName;

    @NotBlank(message = "Email 不能為空")
    @Email(message = "Email 格式不正確")
    private String email;
}