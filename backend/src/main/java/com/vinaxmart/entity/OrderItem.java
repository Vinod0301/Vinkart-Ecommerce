package com.vinaxmart.entity;
import jakarta.persistence.*; import lombok.*;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) private Product product;
 private int quantity;
 private double price;
 @Column(length=200) @Builder.Default private String selectedSize = "";
 @Column(length=500) @Builder.Default private String selectedVariant = "";
}
