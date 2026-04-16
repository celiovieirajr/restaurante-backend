package com.restaurante.backend.dto;

import com.restaurante.backend.entities.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CustomerRequestDTO {
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String cpf;
    
    @NotBlank
    private String phone;
    
    @NotNull
    private LocalDate birthDate;
    
    private Address address;
}
