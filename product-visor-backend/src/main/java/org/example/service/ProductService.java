package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.dto.ProductDto;
import org.example.entity.Category;
import org.example.entity.Product;
import org.example.repository.ProductRepository;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    @SneakyThrows
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }
}
