package com.example.demo.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    @Size(max = 50, message = "姓名長度不可超過 50 字")
    private String name;

    @Size(max = 50, message = "電話長度不可超過 50 字")
    private String phone;

    @Size(max = 50, message = "信用卡號長度不可超過 50 字")
    private String creditCard;

    @Size(max = 50, message = "性別長度不可超過 50 字")
    private String gender;

    private LocalDate birthday;
}