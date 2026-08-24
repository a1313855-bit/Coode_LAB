package com.example.demo.model;

<<<<<<< HEAD
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;

@Entity
@Setter
@Getter
@Table(name="carts")
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    
    @Id
    @Column(name = "cart_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartId;

    /*
    *=================================
    *Cart 1 : 1 User
    *=================================
    */
    @OneToOne( fetch = FetchType.LAZY,targetEntity = User.class)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true)
    @JsonBackReference("user-cart") //之後如果有在DTO設定好，這個Annotation就可以刪除
    private User user;

    @Column(name = "total_quantity",nullable = false)
    private Integer totalQuantity;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /*
    *=================================
    *Cart 1 : N CartItem
    *=================================
    */
    @OneToMany(
        mappedBy = "cart",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        targetEntity = CartItem.class)
    @JsonManagedReference("cart-items")
    private List<CartItem> cartItems = new ArrayList<>();
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
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
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carts")
public class Cart {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @Id
    @Column(name = "cart_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

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

    // 一對一 : One:"Cart" To One:"User"
    @OneToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties("cart")
    private User user;

    // 一對多 : One:"Cart" To Many:"CartItem"
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = CartItem.class, fetch = FetchType.LAZY)
    private List<CartItem> cartItem = new ArrayList<>();

>>>>>>> Maple
}
