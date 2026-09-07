package com.example.demo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VendorResponse {
    private Long vendorId;

    private String vendorName;

    private String email;

    private String status;

    private LocalDateTime activatedAt;

    private LocalDateTime contractExpiresAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
