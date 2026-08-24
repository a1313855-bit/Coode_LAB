package com.example.demo.model;

<<<<<<< HEAD
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

// ========== Jackson ==========
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// ========== Java ==========
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "outfits")
public class Outfit {

    // ╔═══════╗
    // ║ Field ║
    // ╚═══════╝
    @Id
    @Column(name = "outfit_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long outfitId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    // ╔═════════════╗
    // ║ Foreign key ║
    // ╚═════════════╝

    // 一對多 : One:"Outfit" To Many:"OutfitItem"
    @OneToMany(mappedBy = "outfit", cascade = CascadeType.ALL, targetEntity = OutfitItem.class, fetch = FetchType.LAZY)
    private List<OutfitItem> outfitItem = new ArrayList<>();

    // 多對一 : Many="Outfit" To One="User"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties("outfit")
    private User user;

>>>>>>> Maple
}