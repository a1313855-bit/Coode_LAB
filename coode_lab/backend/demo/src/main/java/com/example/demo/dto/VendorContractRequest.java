package com.example.demo.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// 管理員將廠商續約
public class VendorContractRequest {

    @NotNull(message = "合約到期時間不能為空")
    private LocalDateTime contractExpiresAt;
}