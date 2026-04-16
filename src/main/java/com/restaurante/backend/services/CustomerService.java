package com.restaurante.backend.services;

import com.restaurante.backend.dto.CustomerRequestDTO;
import com.restaurante.backend.dto.CustomerResponseDTO;
import com.restaurante.backend.entities.Customer;
import com.restaurante.backend.mapper.CustomerMapper;
import com.restaurante.backend.repositories.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional
    public CustomerResponseDTO create(CustomerRequestDTO dto) {
        Customer customer = customerMapper.toModel(dto);
        customer = customerRepository.save(customer);
        return customerMapper.toResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> findAll() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerResponseDTO findById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id " + id));
        return customerMapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponseDTO update(Long id, CustomerRequestDTO dto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id " + id));
        
        customer.setName(dto.getName());
        customer.setCpf(dto.getCpf());
        customer.setPhone(dto.getPhone());
        customer.setBirthDate(dto.getBirthDate());
        customer.setAddress(dto.getAddress());
        
        customer = customerRepository.save(customer);
        return customerMapper.toResponse(customer);
    }

    @Transactional
    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new IllegalArgumentException("Customer not found with id " + id);
        }
        customerRepository.deleteById(id);
    }
}
