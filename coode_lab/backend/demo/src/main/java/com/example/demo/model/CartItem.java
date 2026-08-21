package com.example.demo.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cart_items",uniqueConstraints = {@UniqueConstraint(name = "uk_cart_product",
    columnNames = {"cart_id","product_id"}
)})
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    /*
     * =================================
     * Primary Key
     * =================================
     */
    @Id
    @Column(name = "cart_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartItemId;

     /*
     * =================================
     * CartItem N : 1 Cart
     * =================================
     */
    @ManyToOne(fetch = FetchType.LAZY,targetEntity = Cart.class)
    @JoinColumn( name = "cart_id",nullable = false )
    @JsonBackReference("cart-items")
    private Cart cart;

    /*
     * =================================
     * CartItem N : 1 Product
     * =================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn( name = "product_id",nullable = false )
    private Product product;


    /*
     * =================================
     * CartItem Information
     * =================================
     */
    @Column(name = "product_quantity")
    private Integer productQuantity;

    @Column(precision = 10, scale = 2,nullable = false)
    private BigDecimal price;

    @Column( name = "total_price", precision = 10, scale = 2,nullable = false)
    private BigDecimal totalPrice;

}
