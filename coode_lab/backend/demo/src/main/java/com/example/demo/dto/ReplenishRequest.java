package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplenishRequest {

    // 本次補貨數量（>0 的整數）
    @NotNull(message = "請輸入本次補貨數量")
    @Min(value = 1, message = "本次補貨數量必須大於 0")
    private Integer quantity;
}
