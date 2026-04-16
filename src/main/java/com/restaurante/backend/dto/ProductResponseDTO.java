package com.restaurante.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProductResponseDTO {
    private Long id;
    private String name;
    private BigDecimal salePrice;
    private BigDecimal costPrice;
    private String imageUrl;
    private CategoryResponseDTO category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
