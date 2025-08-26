package ru.dmitartur.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Table
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String article; // артикул товара
    
    // Store image as byte array in database
    @Lob
    @Column(columnDefinition = "BYTEA")
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.BINARY)
    private byte[] image;
    
    // Keep imageUrl for backward compatibility and external image URLs
    private String imageUrl;
    
    @Column(unique = true)
    private String barcode;

    // Количество по складам хранится в сущности ProductStock

    @ManyToOne
    private Category category;

    @Column(nullable = false)
    private Double price; // цена товара

    @Embedded
    private PackageInfo packageInfo; // информация об упаковке

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductAttributeValue> productAttributeValues;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductMarket> productMarkets;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ProductStock> productStocks;

    // Владелец и компания
    @Column(name = "owner_user_id")
    private UUID ownerUserId;

    @Column(name = "company_id")
    private UUID companyId;

    @PrePersist
    @PreUpdate
    private void prepareAttributeValues() {
        if (productAttributeValues != null) {
            productAttributeValues.forEach(av -> av.setProduct(this));
        }
        if (productMarkets != null) {
            productMarkets.forEach(pm -> pm.setProduct(this));
        }
        if (productStocks != null) {
            // Проставляем обратные ссылки для каскадного сохранения остатков
            for (var stock : productStocks) {
                if (stock != null) {
                    stock.setProduct(this);
                    // Наследуем владельца/компанию если они установлены на товаре
                    if (this.ownerUserId != null && stock.getUserId() == null) {
                        stock.setUserId(this.ownerUserId);
                    }
                }
            }
        }
    }

}
