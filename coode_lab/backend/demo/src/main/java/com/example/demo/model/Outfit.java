package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
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

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "name", length = 100)
    private String name;

    @OneToMany(
        mappedBy = "outfit",
        cascade = CascadeType.PERSIST,
        fetch = FetchType.LAZY
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OutfitItem> outfitItems = new ArrayList<>();
}