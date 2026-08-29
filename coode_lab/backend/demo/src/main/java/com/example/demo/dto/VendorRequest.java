package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// 管理員新增廠商帳號時傳進來的資料。
public class VendorRequest {

    private String vendorName;

    private String email;

    private String password;

}
