package org.example.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.List;

@Entity
@Table
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    
    // Store image as byte array in database
    @Lob
    @Column(columnDefinition = "BYTEA")
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.BINARY)
    private byte[] image;
    
    // Keep imageUrl for backward compatibility and external image URLs
    private String imageUrl;
    
    @Column(unique = true)
    private String barcode;

    private Integer quantity = 0;

    @ManyToOne
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductAttributeValue> productAttributeValues;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductMarket> productMarkets;

    @PrePersist
    @PreUpdate
    private void prepareAttributeValues() {
        System.out.println("PREPARE ATTRIBUTE VALUES");
        if (productAttributeValues != null) {
            productAttributeValues.forEach(av -> av.setProduct(this));
        }
        if (productMarkets != null) {
            productMarkets.forEach(pm -> pm.setProduct(this));
        }
    }

}
