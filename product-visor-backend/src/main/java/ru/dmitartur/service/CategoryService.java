package ru.dmitartur.service;

import lombok.RequiredArgsConstructor;
import ru.dmitartur.entity.Category;
import ru.dmitartur.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<Category> findAll() {
        List<Category> all = categoryRepository.findAll();
        return all;
    }
}
