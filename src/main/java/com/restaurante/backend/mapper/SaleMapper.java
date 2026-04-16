package com.restaurante.backend.mapper;

import com.restaurante.backend.dto.SaleRequestDTO;
import com.restaurante.backend.dto.SaleResponseDTO;
import com.restaurante.backend.entities.Customer;
import com.restaurante.backend.entities.ItemSale;
import com.restaurante.backend.entities.Sale;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SaleMapper {

    private final CustomerMapper customerMapper;
    private final ItemSaleMapper itemSaleMapper;

    public SaleMapper(CustomerMapper customerMapper, ItemSaleMapper itemSaleMapper) {
        this.customerMapper = customerMapper;
        this.itemSaleMapper = itemSaleMapper;
    }

    public Sale toModel(SaleRequestDTO dto) {
        if (dto == null) { return null; }
        Sale sale = new Sale();
        sale.setDiscount(dto.getDiscount());
        
        if (dto.getCustomerId() != null) {
            Customer customer = new Customer();
            customer.setId(dto.getCustomerId());
            sale.setCustomer(customer);
        }

        if (dto.getItems() != null) {
            sale.setItems(dto.getItems().stream().map(itemDto -> {
                ItemSale itemSale = itemSaleMapper.toModel(itemDto);
                itemSale.setSale(sale);
                return itemSale;
            }).collect(Collectors.toList()));
        }

        return sale;
    }

    public SaleResponseDTO toResponse(Sale entity) {
        if (entity == null) { return null; }
        SaleResponseDTO dto = new SaleResponseDTO();
        dto.setId(entity.getId());
        dto.setDiscount(entity.getDiscount());
        dto.setTotalValue(entity.getTotalValue());
        dto.setDeliveryType(entity.getDeliveryType() != null ? entity.getDeliveryType().name() : null);
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getCustomer() != null) {
            dto.setCustomer(customerMapper.toResponse(entity.getCustomer()));
        }

        if (entity.getItems() != null) {
            dto.setItems(entity.getItems().stream()
                .map(itemSaleMapper::toResponse)
                .collect(Collectors.toList()));
        }

        return dto;
    }
}
