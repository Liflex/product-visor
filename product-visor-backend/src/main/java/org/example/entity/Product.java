package org.example.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String imageUrl;

    @ManyToOne
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductAttributeValue> productAttributeValues;

    @PrePersist
    @PreUpdate
    private void prepareAttributeValues() {
        if (productAttributeValues != null) {
            productAttributeValues.forEach(av -> av.setProduct(this));
        }
    }

}
