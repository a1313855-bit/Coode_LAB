package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserResponse {

    private Long userId;
    private String email;
    private String name;
    private String phone;
    private String creditCard;
    private String status;
    private String gender;
    private String picture;
    private LocalDate birthday;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
