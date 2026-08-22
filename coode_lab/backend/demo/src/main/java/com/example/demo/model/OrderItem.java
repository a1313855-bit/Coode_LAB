package com.example.demo.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {
    // 欄位
    @Id
    @Column(name = "order_item_id")
    private Integer orderItemId;
    @Column(name = "product_quantity")
    private Integer productQuantity;
    @Column(name = "price")
    private BigDecimal price;
    @Column(name = "price_total")
    private BigDecimal priceTotal;
    @Column(name = "status")
    private String status;

    // 關聯欄位
    // 多對一關聯
    @ManyToOne(fetch = FetchType.LAZY) // 多對一：多 orderItem 一 order
    @JoinColumn(name = "order_id") // 資料庫中的外鍵欄位名稱
    @JsonIgnoreProperties("orderItem") // 忽略 order 物件中的 orderItem
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY) // 多對一：多 orderItem 一 vendor
    @JoinColumn(name = "vendor_id") // 資料庫中的外鍵欄位名稱
    @JsonIgnoreProperties("orderItem") // 忽略 vendor 物件中的 orderItem
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY) // 多對一：多 orderItem 一 product
    @JoinColumn(name = "product_id") // 資料庫中的外鍵欄位名稱
    @JsonIgnoreProperties("orderItem") // 忽略 product 物件中的 orderItem
    private Product product;

    // 一對多關聯
    // mappedBy = "orderItem"：指向 ReturnItem.class 中 @ManyToOne 欄位的「屬性名稱」
    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.PERSIST, targetEntity = ReturnItem.class, fetch = FetchType.LAZY)
    private List<ReturnItem> returnItem = new ArrayList<ReturnItem>();
}