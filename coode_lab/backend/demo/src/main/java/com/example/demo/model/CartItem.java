package com.example.demo.model;

// ========== lombok ==========
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// ========== Jakarta Persistence（JPA） ==========
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;

// ========== Jackson ==========
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// ========== Java ==========
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cart_product", columnNames = { "cart_id", "product_id" }) })
public class CartItem {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @Id
    @Column(name = "cart_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    @Column(name = "product_quantity", nullable = false)
    private Integer productQuantity;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    // ╔═════════════╗
    // ║ Foreign key ║
    // ╚═════════════╝

    // 多對一 : Many="CartItem" To One="Cart"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnoreProperties("cartItem")
    private Cart cart;

    // 多對一 : Many="CartItem" To One="Product"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties("cartItem")
    private Product product;

}
