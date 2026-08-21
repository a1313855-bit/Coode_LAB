package com.example.demo.model;

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
   


}
