package com.example.demo.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    private String name;

    private String phone;

    private String creditCard;

    private String gender;

    private LocalDate birthday;
}