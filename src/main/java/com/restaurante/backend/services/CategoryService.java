package com.restaurante.backend.services;

import com.restaurante.backend.dto.CategoryRequestDTO;
import com.restaurante.backend.dto.CategoryResponseDTO;
import com.restaurante.backend.entities.Category;
import com.restaurante.backend.mapper.CategoryMapper;
import com.restaurante.backend.repositories.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO dto) {
        Category category = categoryMapper.toModel(dto);
        category = categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id " + id));
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponseDTO update(Long id, CategoryRequestDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id " + id));
        category.setName(dto.getName());
        category = categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category not found with id " + id);
        }
        categoryRepository.deleteById(id);
    }
}
