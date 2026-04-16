package com.restaurante.backend.mapper;

import com.restaurante.backend.dto.CustomerRequestDTO;
import com.restaurante.backend.dto.CustomerResponseDTO;
import com.restaurante.backend.entities.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toModel(CustomerRequestDTO dto) {
        if (dto == null) { return null; }
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setCpf(dto.getCpf());
        customer.setPhone(dto.getPhone());
        customer.setBirthDate(dto.getBirthDate());
        customer.setAddress(dto.getAddress());
        return customer;
    }

    public CustomerResponseDTO toResponse(Customer entity) {
        if (entity == null) { return null; }
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCpf(entity.getCpf());
        dto.setPhone(entity.getPhone());
        dto.setBirthDate(entity.getBirthDate());
        dto.setAddress(entity.getAddress());
        return dto;
    }
}
