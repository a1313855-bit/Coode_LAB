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
public class VariantBatchStatusRequest {

    // 依顏色整批套用（黑不賣、白照賣）— 可為空
    private String color;

    // 依尺寸整批套用（M 不賣、L 照賣）— 可為空
    private String size;

    // 目標狀態：ACTIVE = 可販售；INACTIVE = 停售
    @NotBlank(message = "狀態不能為空")
    private String status;
}