package ru.dmitartur.controller;

import lombok.RequiredArgsConstructor;
import ru.dmitartur.dto.CategoryDto;
import ru.dmitartur.mapper.CategoryMapper;
import ru.dmitartur.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAll() {
        return ResponseEntity.ok(categoryService.findAll().stream().map(categoryMapper::categoryToCategoryDto).toList());
    }
}
