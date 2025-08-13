package ru.dmitartur.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "markets")
@Data
public class Market {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    // Store image as byte array in database
    @Lob
    @Column(columnDefinition = "BYTEA")
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.BINARY)
    private byte[] image;
} 