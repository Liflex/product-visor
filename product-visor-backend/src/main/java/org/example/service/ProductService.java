package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.ProductDto;
import org.example.entity.Category;
import org.example.entity.Product;
import org.example.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository productRepository;

    public Product save(Product entity) {
        return productRepository.save(entity);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }
}
