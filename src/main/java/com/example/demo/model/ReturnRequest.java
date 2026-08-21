package com.example.demo.model;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "return_requests")
public class ReturnRequest {
    // 欄位
    @Id
    @Column(name = "return_requests_id")
    private Integer returnRequestsId;
    @Column(name = "status")
    private String status;
    @Column(name = "request_type")
    private String requestType;
    @Column(name = "return_request_quantity")
    private Integer returnRequestQuantity;
    @Column(name = "create_at")
    private LocalDateTime createAt;

    // 關聯欄位
    // 多對一關聯
    @ManyToOne(fetch = FetchType.LAZY) // // 多對一：多 ReturnRequest 一 Order
    @JoinColumn(name = "order_id") // 資料庫中的外鍵欄位名稱
    @JsonIgnoreProperties("returnRequest") // 忽略 orderItem 物件中的 returnRequest
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY) // 多對一：多 ReturnRequest 一 User
    @JoinColumn(name = "user_id") // 資料庫中的外鍵欄位名稱
    @JsonIgnoreProperties("returnRequest") // 忽略 orderItem 物件中的 returnRequest
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // 多對一：多 ReturnRequest 一 Vendor
    @JoinColumn(name = "vendor_id") // 資料庫中的外鍵欄位名稱
    @JsonIgnoreProperties("returnRequest") // 忽略 orderItem 物件中的 returnRequest
    private Vendor vendor;

    // 一對多關聯
    // mappedBy = "returnRequest"：指向 ReturnItem.class 中 @ManyToOne 欄位的「屬性名稱」
    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.PERSIST, targetEntity = ReturnItem.class, fetch = FetchType.LAZY)
    private List<ReturnItem> returnItem = new ArrayList<ReturnItem>();
}
