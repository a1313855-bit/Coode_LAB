package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockRequest {

    @NotNull(message = "庫存數量不能為空")
    @Min(value = 0, message = "庫存數量不能小於 0")
    private Integer stock;
}