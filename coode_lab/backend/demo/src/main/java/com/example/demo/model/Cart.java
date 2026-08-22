package com.example.demo.model;

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
}
