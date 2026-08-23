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

// ========== Jackson ==========
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// ========== Java ==========
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "return_requests")
public class ReturnRequest {

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
    private LocalDateTime createAt;

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

}
