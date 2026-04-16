package com.restaurante.backend.dto;

import com.restaurante.backend.entities.Address;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CustomerResponseDTO {
    private Long id;
    private String name;
    private String cpf;
    private String phone;
    private LocalDate birthDate;
    private Address address;
}
