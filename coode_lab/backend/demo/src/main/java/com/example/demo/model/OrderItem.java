package com.example.demo.model;

<<<<<<< HEAD
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
=======
// ========== lombok ==========
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// ========== Jakarta Persistence（JPA） ==========
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

// ========== Jackson ==========
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// ========== Java ==========
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

@Getter
@Setter
>>>>>>> Maple
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {
<<<<<<< HEAD
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
=======

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @Id
    @Column(name = "order_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @Column(name = "product_quantity", nullable = false)
    private Integer productQuantity;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "price_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceTotal;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    // ╔═════════════╗
    // ║ Foreign key ║
    // ╚═════════════╝

    // 一對多 : One:"OrderItem" To Many:"ReturnItem"
    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, targetEntity = ReturnItem.class, fetch = FetchType.LAZY)
    private List<ReturnItem> returnItem = new ArrayList<ReturnItem>();

    // 多對一 : Many="OrderItem" To One="Order"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties("orderItem")
    private Order order;

    // 多對一 : Many="OrderItem" To One="Vendor"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnoreProperties("orderItem")
    private Vendor vendor;

    // 多對一 : Many="OrderItem" To One="Product"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties("orderItem")
    private Product product;

>>>>>>> Maple
}