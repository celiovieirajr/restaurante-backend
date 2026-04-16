package com.restaurante.backend.controllers;

import com.restaurante.backend.dtos.ViaCepResponseDTO;
import com.restaurante.backend.services.ViaCepService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/cep")
@RequiredArgsConstructor
public class ViaCepController {

    private final ViaCepService viaCepService;

    @GetMapping("/{cep}")
    public Mono<ViaCepResponseDTO> getCep(@PathVariable String cep) {
        return viaCepService.getAddressByCep(cep);
    }
}
