package com.example.demo.model;

<<<<<<< HEAD
import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
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

// ========== hibernate ==========
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// ========== Jackson ==========
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// ========== Java ==========
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
>>>>>>> Maple
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
<<<<<<< HEAD
    // 欄位
    @Id
    @Column(name = "order_id")
    private Integer orderId;
    @Column(name = "recipient_name")
    private String recipientName;
    @Column(name = "recipient_phone")
    private String recipientPhone;
    @Column(name = "recipient_address")
    private String recipientAddress;
    @Column(name = "total_amount")
    private Integer totalAmount;
    @Column(name = "sum_total")
    private BigDecimal sumTotal;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp // Hibernate 在第一次 save() 時自動填入當下時間
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateAt;

    // 關聯欄位
    // 多對一關聯
    @ManyToOne(fetch = FetchType.LAZY) // 多對一：多 order 一 user
    @JoinColumn(name = "user_id") // 資料庫中的外鍵欄位名稱
    @JsonIgnoreProperties("order") // 忽略 order 物件中的 orderItem
    private User user;

    // 一對多關聯
    // mappedBy = "order"：指向 OrderItem.class 中 @ManyToOne 欄位的「屬性名稱」
    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST, targetEntity = OrderItem.class, fetch = FetchType.LAZY)
    private List<OrderItem> orderItem = new ArrayList<OrderItem>();

    // mappedBy = "order"：指向 ReturnRequest.class 中 @ManyToOne 欄位的「屬性名稱」
    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST, targetEntity = ReturnRequest.class, fetch = FetchType.LAZY)
    private List<ReturnRequest> returnRequest = new ArrayList<ReturnRequest>();
=======

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(name = "recipient_name", length = 100, nullable = false)
    private String recipientName;

    @Column(name = "recipient_phone", length = 100, nullable = false)
    private String recipientPhone;

    @Column(name = "recipient_address", length = 255, nullable = false)
    private String recipientAddress;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "sum_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal sumTotal;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ╔═════════════╗
    // ║ Foreign key ║
    // ╚═════════════╝

    // 一對多 : One:"Order" To Many:"OrderItem"
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, targetEntity = OrderItem.class, fetch = FetchType.LAZY)
    private List<OrderItem> orderItem = new ArrayList<OrderItem>();

    // 一對多 : One:"Order" To Many:"ReturnRequest"
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, targetEntity = ReturnRequest.class, fetch = FetchType.LAZY)
    private List<ReturnRequest> returnRequest = new ArrayList<ReturnRequest>();

    // 多對一 : Many="Order" To One="User"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties("order")
    private User user;

>>>>>>> Maple
}
