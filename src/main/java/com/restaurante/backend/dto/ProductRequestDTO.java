package com.restaurante.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequestDTO {

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal salePrice;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal costPrice;

    private String imageUrl;

    @NotNull
    private Long categoryId;
}
