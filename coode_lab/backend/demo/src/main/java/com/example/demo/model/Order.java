package com.example.demo.model;

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
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

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
    @Column(name = "update_at")
    private LocalDateTime updateAt;

    // ╔═════════════╗
    // ║ Foreign key ║
    // ╚═════════════╝

    // 一對多 : One:"Order" To Many:"OrderItem"
    @OneToMany(mappedBy = "order", targetEntity = OrderItem.class, fetch = FetchType.LAZY)
    private List<OrderItem> orderItem = new ArrayList<OrderItem>();

    // 一對多 : One:"Order" To Many:"ReturnRequest"
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, targetEntity = ReturnRequest.class, fetch = FetchType.LAZY)
    private List<ReturnRequest> returnRequest = new ArrayList<ReturnRequest>();

    // 多對一 : Many="Order" To One="User"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties("order")
    private User user;

}
