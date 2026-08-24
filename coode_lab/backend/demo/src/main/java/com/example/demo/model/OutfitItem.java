package com.example.demo.model;

<<<<<<< HEAD
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "outfit_items")
@Data
@NoArgsConstructor
public class OutfitItem {

    @Id
    @Column(name = "outfititems_id")
    private Integer outfititemsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outfit_id", nullable = false)
    @JsonIgnoreProperties("outfitItems")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Outfit outfit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties("outfitItems")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    @Column(name = "slot_type", length = 45)
    private String slotType;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;

// ========== Jackson ==========
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "outfit_items")
public class OutfitItem {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @Id
    @Column(name = "outfititems_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long outfititemsId;

    @Column(name = "slot_type", length = 100, nullable = false)
    private String slotType;

    // ╔═════════════╗
    // ║ Foreign key ║
    // ╚═════════════╝

    // 多對一 : Many="OutfitItem" To One="Product"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties("outfitItem")
    private Product product;

    // 多對一 : Many="OutfitItem" To One="Outfit"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outfit_id", nullable = false)
    @JsonIgnoreProperties("outfitItem")
    private Outfit outfit;

>>>>>>> Maple
}