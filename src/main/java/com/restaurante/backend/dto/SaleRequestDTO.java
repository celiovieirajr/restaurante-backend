package com.restaurante.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SaleRequestDTO {

    private Long customerId;

    private String customerName;

    private String customerPhone;

    private String customerAddress;

    @DecimalMin("0.0")
    private BigDecimal discount;

    private String deliveryType;

    private List<ItemSaleRequestDTO> items;
}
