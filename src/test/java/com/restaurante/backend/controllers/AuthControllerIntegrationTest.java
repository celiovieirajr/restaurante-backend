package com.restaurante.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.backend.dto.LoginRequestDTO;
import com.restaurante.backend.dto.RefreshRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("homolog")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.restaurante.backend.repositories.UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        String hash = passwordEncoder.encode("password123");
        userRepository.findAll().forEach(u -> {
            u.setPasswordHash(hash);
            userRepository.save(u);
        });
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        System.out.println("Users in DB: " + userRepository.count());
        userRepository.findAll().forEach(u -> System.out.println("User: " + u.getUsername() + " hash: " + u.getPasswordHash()));
        
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("usuario");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.role").value("USUARIO"));
    }

    @Test
    void shouldReturn401OnInvalidCredentials() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("wrong");
        loginRequest.setPassword("wrong");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRefreshSuccessfully() throws Exception {
        // 1. Login
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("usuario");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String responseStr = loginResult.getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(responseStr).get("refreshToken").asText();

        // 2. Refresh
        RefreshRequestDTO refreshRequest = new RefreshRequestDTO();
        refreshRequest.setRefreshToken(refreshToken);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void shouldAccessProtectedEndpoint() throws Exception {
        // 1. Login
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("administrador");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String responseStr = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseStr).get("accessToken").asText();

        // 2. Access Admin Dashboard
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyAccessToUsuarioOnAdminEndpoint() throws Exception {
        // 1. Login as Usuario
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("usuario");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String responseStr = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseStr).get("accessToken").asText();

        // 2. Access Admin Dashboard -> Expect 403
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }
}
