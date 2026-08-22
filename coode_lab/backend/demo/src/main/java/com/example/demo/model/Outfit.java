package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "outfits")
@Data
@NoArgsConstructor
public class Outfit {

    @Id
    @Column(name = "outfit_id")
    private Integer outfitId;

    @Column(name = "name", length = 100)
    private String name;

    @OneToMany(
        mappedBy = "outfit",
        cascade = CascadeType.PERSIST,
        fetch = FetchType.LAZY
    )

    @ManyToOne(fetch = FetchType.LAZY) // 多對一：多 outfit 一 user
    @JoinColumn(name = "user_id") // 資料庫中的外鍵欄位名稱
    @JsonIgnoreProperties("outfit") // 忽略 user 物件中的 outfit
    private User user;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "outfit", cascade = CascadeType.PERSIST, targetEntity = OutfitItem.class, fetch = FetchType.LAZY)
    private List<OutfitItem> outfitItems = new ArrayList<>();
}