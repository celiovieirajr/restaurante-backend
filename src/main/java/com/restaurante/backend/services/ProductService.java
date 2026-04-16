package com.restaurante.backend.services;

import com.restaurante.backend.dto.ProductRequestDTO;
import com.restaurante.backend.dto.ProductResponseDTO;
import com.restaurante.backend.entities.Product;
import com.restaurante.backend.entities.Category;
import com.restaurante.backend.mapper.ProductMapper;
import com.restaurante.backend.repositories.ProductRepository;
import com.restaurante.backend.repositories.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductResponseDTO create(ProductRequestDTO dto) {
        if (!categoryRepository.existsById(dto.getCategoryId())) {
            throw new IllegalArgumentException("Category not found with id " + dto.getCategoryId());
        }
        Product product = productMapper.toModel(dto);
        product = productRepository.save(product);
        return productMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id " + id));
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponseDTO update(Long id, ProductRequestDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id " + id));
        
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id " + dto.getCategoryId()));

        product.setName(dto.getName());
        product.setSalePrice(dto.getSalePrice());
        product.setCostPrice(dto.getCostPrice());
        product.setImageUrl(dto.getImageUrl());
        product.setCategory(category);
        
        product = productRepository.save(product);
        return productMapper.toResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with id " + id);
        }
        productRepository.deleteById(id);
    }
}
