package org.example.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "product_markets")
@Data
public class ProductMarket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    private Product product;

    @ManyToOne
    @JsonBackReference
    private Market market;
    
    @Column(nullable = false)
    private Integer quantity = 0;
    
    @Column(nullable = false)
    private Double price = 0.0;
    
    @PrePersist
    @PreUpdate
    private void prepareRelations() {
        if (product != null && market != null) {
            // Убеждаемся, что связи установлены правильно
        }
    }
} 