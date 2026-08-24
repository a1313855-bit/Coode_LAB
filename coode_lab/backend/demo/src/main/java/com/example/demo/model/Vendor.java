package com.example.demo.model;

<<<<<<< HEAD
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vendor {
   
    @Id
    @Column(name="vendor_id")
    private Integer vendorId;

    @Column(name="vendor_name",length=100)
    private String vendorName;
    
    @Column(name="email",length=100)
    private String email;

    @Column(name="password",length=255)
    private String password;

    @Column(name="status",length=45)
    private String status;

    @Column(name="activated_at")
    private LocalDateTime activatedAt;

    @Column(name="contract_expires_at")
    private LocalDateTime contractExpiresAt;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy= "vendor", cascade=CascadeType.ALL,fetch=FetchType.LAZY)
    private List<Product> products=new ArrayList<>();

    @OneToMany(mappedBy= "vendor", cascade=CascadeType.ALL,fetch=FetchType.LAZY)
    private List<OrderItem> orderItems=new ArrayList<>();

    @OneToMany(mappedBy= "vendor", cascade=CascadeType.ALL,fetch=FetchType.LAZY)
    private List<ReturnRequest> returnRequests=new ArrayList<>();
   

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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

// ========== hibernate ==========
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// ========== Jackson ==========
import com.fasterxml.jackson.annotation.JsonFormat;

// ========== Java ==========
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vendors")
public class Vendor {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @Id
    @Column(name = "vendor_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vendorId;

    @Column(name = "vendor_name", length = 100, nullable = false)
    private String vendorName;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "contract_expires_at")
    private LocalDateTime contractExpiresAt;

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

    // 一對多 : One:"Vendor" To Many:"Product"
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, targetEntity = Product.class, fetch = FetchType.LAZY)
    private List<Product> product = new ArrayList<>();

    // 一對多 : One:"Vendor" To Many:"OrderItem"
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, targetEntity = OrderItem.class, fetch = FetchType.LAZY)
    private List<OrderItem> orderItem = new ArrayList<>();

    // 一對多 : One:"Vendor" To Many:"ReturnRequest"
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, targetEntity = ReturnRequest.class, fetch = FetchType.LAZY)
    private List<ReturnRequest> returnRequest = new ArrayList<>();
>>>>>>> Maple

}
