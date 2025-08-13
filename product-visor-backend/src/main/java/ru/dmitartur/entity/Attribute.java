package ru.dmitartur.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table
@Data
public class Attribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String nameRus;
    private String type;
    private boolean required;
    @Column(columnDefinition = "boolean default false")
    private boolean multiple;

    @ManyToOne
    @JsonBackReference
    private Category category;

}
