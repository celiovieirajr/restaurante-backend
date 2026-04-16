package com.restaurante.backend.mapper;

import com.restaurante.backend.dto.ItemSaleRequestDTO;
import com.restaurante.backend.dto.ItemSaleResponseDTO;
import com.restaurante.backend.entities.ItemSale;
import com.restaurante.backend.entities.Product;
import org.springframework.stereotype.Component;

@Component
public class ItemSaleMapper {

    public ItemSale toModel(ItemSaleRequestDTO dto) {
        if (dto == null) { return null; }
        ItemSale itemSale = new ItemSale();
        itemSale.setQuantity(dto.getQuantity());
        itemSale.setDiscount(dto.getDiscount());
        
        if (dto.getProductId() != null) {
            Product product = new Product();
            product.setId(dto.getProductId());
            itemSale.setProduct(product);
        }
        return itemSale;
    }

    public ItemSaleResponseDTO toResponse(ItemSale entity) {
        if (entity == null) { return null; }
        ItemSaleResponseDTO dto = new ItemSaleResponseDTO();
        dto.setId(entity.getId());
        dto.setQuantity(entity.getQuantity());
        dto.setUnitValue(entity.getUnitValue());
        dto.setDiscount(entity.getDiscount());
        dto.setTotalValue(entity.getTotalValue());

        if (entity.getProduct() != null) {
            dto.setProductId(entity.getProduct().getId());
            dto.setProductName(entity.getProduct().getName());
        }
        return dto;
    }
}
