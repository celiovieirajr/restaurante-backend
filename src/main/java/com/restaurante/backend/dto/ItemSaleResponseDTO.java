package com.restaurante.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ItemSaleResponseDTO {
    private Long id;
    private Integer quantity;
    private BigDecimal unitValue;
    private BigDecimal discount;
    private BigDecimal totalValue;
    private Long productId;
    private String productName;
}
