package com.restaurante.backend.mapper;

import com.restaurante.backend.dto.CategoryRequestDTO;
import com.restaurante.backend.dto.CategoryResponseDTO;
import com.restaurante.backend.entities.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toModel(CategoryRequestDTO dto) {
        if (dto == null) { return null; }
        Category category = new Category();
        category.setName(dto.getName());
        return category;
    }

    public CategoryResponseDTO toResponse(Category entity) {
        if (entity == null) { return null; }
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
