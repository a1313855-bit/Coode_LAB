package com.example.demo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "return_items")
public class ReturnItem {
    // 欄位
    @Id
    @Column(name = "return_item_id")
    private Integer returnItemId;
    @Column(name = "status")
    private String status;
    @Column(name = "reason")
    private String reason;
    @Column(name = "description")
    private String description;
    @Column(name = "returned_quantity")
    private Integer returnedQuantity;
    @Column(name = "exchanged_quantity")
    private Integer exchangedQuantity;
    @Column(name = "rejected_quantity")
    private Integer rejectedQuantity;
    @Column(name = "refund")
    private BigDecimal refund;

    // 關聯欄位
    // 多對一關聯
    @ManyToOne(fetch = FetchType.LAZY) // 多對一：多 returnItem 一 orderItem
    @JoinColumn(name = "order_item_id") // 資料庫中的外鍵欄位名稱
    @JsonIgnoreProperties("returnItem") // 忽略 orderItem 物件中的 returnItem
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY) // 多對一：多 ReturnItem 一 ReturnRequest
    @JoinColumn(name = "return_requests_id") // 資料庫中的外鍵欄位名稱
    @JsonIgnoreProperties("returnItem") // 忽略 product 物件中的 returnItem
    private ReturnRequest returnRequest;
}
