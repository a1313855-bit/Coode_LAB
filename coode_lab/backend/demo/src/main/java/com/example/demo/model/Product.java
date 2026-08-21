package com.example.demo.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

@Entity
@Table(name="products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @Column(name="product_id")
    private Integer productId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="vendor_id",nullable=false)
    private Vendor vendor;

    @Column(name="name",length=100)
    private String name;

    @Column(name="pattern",length=100)
    private String pattern;

    @Column(name="category_type",length=100)
    private String categoryType;

    @Column(name="style",length=100)
    private String style;

    @Column(name="color",length=45)
    private String color;

    @Column(name="size",length=45)
    private String size;

    @Column(name="stock")
    private Integer stock;

    @Column(name="price",precision=10,scale=2)
    private BigDecimal price;

    @Column(name="description",length=500)
    private String description;

    @Column(name="images_jpg",length=255)
    private String imagesJpg;

    @Column(name="outfit_png",length=255)
    private String outfitPng;

    @OneToMany(mappedBy= "product", cascade=CascadeType.ALL,fetch=FetchType.LAZY)
    private List<CartItem> cartItems=new ArrayList<>();

    @OneToMany(mappedBy= "product", cascade=CascadeType.ALL,fetch=FetchType.LAZY)
    private List<OutfitItem> outfitItems=new ArrayList<>();

    @OneToMany(mappedBy= "product", cascade=CascadeType.ALL,fetch=FetchType.LAZY)
    private List<OrderItem> orderItems=new ArrayList<>();
    
}
