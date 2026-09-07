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
public class VendorActivateRequest {

        @NotNull(message = "啟用時間不能為空")
        private LocalDateTime activatedAt;

        @NotNull(message = "合約到期時間不能為空")
        private LocalDateTime contractExpiresAt;

}
