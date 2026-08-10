package com.vinaxmart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 3000)
    private String description;

    private double price;
    private double discount;
    private int stock;

    // Kept for backward compatibility. It stores the primary/first image.
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", length = 2000)
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    /*
     * Flexible category-specific specifications.
     *
     * Examples:
     * Mobile: RAM=8GB / 12GB, Storage=128GB / 256GB, Color=Black
     * Laptop: RAM=16GB, Storage=512GB SSD, Processor=Core i7
     * Clothing: Available Sizes=S, M, L, XL, Color=Black, Material=Cotton
     *
     * Keeping these as a map means the admin can add any future specification
     * without changing the database schema or Java entity again.
     */
    @ElementCollection
    @CollectionTable(name = "product_specifications", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "spec_name", length = 100)
    @Column(name = "spec_value", length = 2000)
    @Builder.Default
    private Map<String, String> specifications = new LinkedHashMap<>();

    private double rating;

    @ManyToOne
    private Category category;

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
        if ((this.imageUrl == null || this.imageUrl.isBlank()) && !this.imageUrls.isEmpty()) {
            this.imageUrl = this.imageUrls.get(0);
        }
    }

    public void setSpecifications(Map<String, String> specifications) {
        this.specifications = specifications == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(specifications);
    }
}
