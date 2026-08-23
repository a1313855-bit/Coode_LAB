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

// ========== Jackson ==========
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// ========== Java ==========
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "pattern", length = 100)
    private String pattern;

    @Column(name = "category_type", length = 100)
    private String categoryType;

    @Column(name = "style", length = 100)
    private String style;

    @Column(name = "color", length = 50, nullable = false)
    private String color;

    @Column(name = "size", length = 50, nullable = false)
    private String size;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "images_jpg", length = 255)
    private String imagesJpg;

    @Column(name = "outfit_png", length = 255)
    private String outfitPng;

    // ╔═════════════╗
    // ║ Foreign key ║
    // ╚═════════════╝

    // 一對多 : One:"Product" To Many:"CartItem"
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, targetEntity = CartItem.class, fetch = FetchType.LAZY)
    private List<CartItem> cartItems = new ArrayList<>();

    // 一對多 : One:"Product" To Many:"OutfitItem"
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, targetEntity = OutfitItem.class, fetch = FetchType.LAZY)
    private List<OutfitItem> outfitItems = new ArrayList<>();

    // 一對多 : One:"Product" To Many:"OrderItem"
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, targetEntity = OrderItem.class, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    // 多對一 : Many="Product" To One="Vendor"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnoreProperties("product")
    private Vendor vendor;

}
