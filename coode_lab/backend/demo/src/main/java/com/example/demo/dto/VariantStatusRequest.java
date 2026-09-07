package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VariantStatusRequest {

    @NotBlank(message = "狀態不能為空")
    private String status;
}