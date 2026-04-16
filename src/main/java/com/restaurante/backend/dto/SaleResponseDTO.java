package com.restaurante.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SaleResponseDTO {
    private Long id;
    private BigDecimal discount;
    private BigDecimal totalValue;
    private CustomerResponseDTO customer;
    private List<ItemSaleResponseDTO> items;
    private String deliveryType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
