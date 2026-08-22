package com.example.demo.model;

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
}