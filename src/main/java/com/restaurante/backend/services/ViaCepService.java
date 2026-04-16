package com.restaurante.backend.services;

import com.restaurante.backend.dtos.ViaCepResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ViaCepService {

    private final WebClient webClient;

    public ViaCepService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://viacep.com.br/ws").build();
    }

    public Mono<ViaCepResponseDTO> getAddressByCep(String cep) {
        String cleanCep = cep.replaceAll("[^\\d]", "");
        return this.webClient.get()
                .uri("/{cep}/json/", cleanCep)
                .retrieve()
                .bodyToMono(ViaCepResponseDTO.class)
                .filter(response -> response.getErro() == null || !response.getErro().equals("true"))
                .switchIfEmpty(Mono.error(new RuntimeException("CEP não encontrado")));
    }
}
