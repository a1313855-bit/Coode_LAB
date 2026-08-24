package com.example.demo.model;

<<<<<<< HEAD
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

// ========== Jackson ==========
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// ========== Java ==========
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

@Getter
@Setter
>>>>>>> Maple
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "return_requests")
public class ReturnRequest {
<<<<<<< HEAD
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
=======

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @Id
    @Column(name = "return_requests_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long returnRequestsId;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "request_type", length = 50, nullable = false)
    private String requestType;

    @Column(name = "return_request_quantity", nullable = false)
    private Integer returnRequestQuantity;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ╔═════════════╗
    // ║ Foreign key ║
    // ╚═════════════╝

    // 一對多 : One:"ReturnRequest" To Many:"ReturnItem"
    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, targetEntity = ReturnItem.class, fetch = FetchType.LAZY)
    private List<ReturnItem> returnItem = new ArrayList<ReturnItem>();

    // 多對一 : Many="ReturnRequest" To One="Order"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties("returnRequest")
    private Order order;

    // 多對一 : Many="ReturnRequest" To One="User"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties("returnRequest")
    private User user;

    // 多對一 : Many="ReturnRequest" To One="Vendor"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnoreProperties("returnRequest")
    private Vendor vendor;

>>>>>>> Maple
}
