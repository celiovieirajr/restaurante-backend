package com.restaurante.backend.mapper;

import com.restaurante.backend.dto.ProductRequestDTO;
import com.restaurante.backend.dto.ProductResponseDTO;
import com.restaurante.backend.entities.Category;
import com.restaurante.backend.entities.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    private final CategoryMapper categoryMapper;

    public ProductMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public Product toModel(ProductRequestDTO dto) {
        if (dto == null) { return null; }
        Product product = new Product();
        product.setName(dto.getName());
        product.setSalePrice(dto.getSalePrice());
        product.setCostPrice(dto.getCostPrice());
        product.setImageUrl(dto.getImageUrl());

        if (dto.getCategoryId() != null) {
            Category category = new Category();
            category.setId(dto.getCategoryId());
            product.setCategory(category);
        }
        return product;
    }

    public ProductResponseDTO toResponse(Product entity) {
        if (entity == null) { return null; }
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setSalePrice(entity.getSalePrice());
        dto.setCostPrice(entity.getCostPrice());
        dto.setImageUrl(entity.getImageUrl());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getCategory() != null) {
            dto.setCategory(categoryMapper.toResponse(entity.getCategory()));
        }
        return dto;
    }
}
