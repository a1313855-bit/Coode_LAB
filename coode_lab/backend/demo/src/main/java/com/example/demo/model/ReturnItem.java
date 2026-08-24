package com.example.demo.model;

<<<<<<< HEAD
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;

// ========== Jackson ==========
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// ========== Java ==========
import java.math.BigDecimal;

@Getter
@Setter
>>>>>>> Maple
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "return_items")
public class ReturnItem {
<<<<<<< HEAD
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
=======

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @Id
    @Column(name = "return_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long returnItemId;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "returned_quantity", nullable = false)
    private Integer returnedQuantity;

    @Column(name = "exchanged_quantity", nullable = false)
    private Integer exchangedQuantity;

    @Column(name = "rejected_quantity", nullable = false)
    private Integer rejectedQuantity;

    @Column(name = "refund", precision = 10, scale = 2, nullable = false)
    private BigDecimal refund;

    // ╔═════════════╗
    // ║ Foreign key ║
    // ╚═════════════╝

    // 多對一 : Many="ReturnItem" To One="OrderItem"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    @JsonIgnoreProperties("returnItem")
    private OrderItem orderItem;

    // 多對一 : Many="ReturnItem" To One="ReturnRequest"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_requests_id", nullable = false)
    @JsonIgnoreProperties("returnItem")
>>>>>>> Maple
    private ReturnRequest returnRequest;
}
