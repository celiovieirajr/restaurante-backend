package com.restaurante.backend.services;

import com.restaurante.backend.dto.UserRequestDTO;
import com.restaurante.backend.dto.UserResponseDTO;
import com.restaurante.backend.entities.User;
import com.restaurante.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != com.restaurante.backend.entities.enums.Role.MASTER)
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (dto.getRole() == com.restaurante.backend.entities.enums.Role.MASTER) {
            throw new RuntimeException("Cannot create a MASTER user through this endpoint.");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setEnabled(true);

        // Default Permissions
        java.util.Set<String> perms = new java.util.HashSet<>();
        if (dto.getRole() == com.restaurante.backend.entities.enums.Role.ADMINISTRADOR) {
            // Admin gets everything by default
            String[] modules = {"CLIENTE", "PRODUTO", "CATEGORIA", "VENDA", "USUARIO"};
            String[] actions = {"VIEW", "ADD", "EDIT", "DELETE"};
            for (String m : modules) for (String a : actions) perms.add(m + "_" + a);
        } else {
            // Regular user gets only View
            String[] modules = {"CLIENTE", "PRODUTO", "CATEGORIA", "VENDA", "USUARIO"};
            for (String m : modules) perms.add(m + "_VIEW");
        }
        user.setPermissions(perms);
        
        return convertToResponseDTO(userRepository.save(user));
    }

    @Transactional
    public UserResponseDTO updatePermissions(Long id, java.util.Set<String> permissions) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == com.restaurante.backend.entities.enums.Role.MASTER) {
            throw new RuntimeException("Cannot modify MASTER user permissions.");
        }

        user.setPermissions(permissions);
        return convertToResponseDTO(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() == com.restaurante.backend.entities.enums.Role.MASTER) {
            throw new RuntimeException("Cannot delete a MASTER user.");
        }
        
        userRepository.deleteById(id);
    }

    @Transactional
    public void updatePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == com.restaurante.backend.entities.enums.Role.MASTER) {
            throw new RuntimeException("Cannot update MASTER user password via this method.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private UserResponseDTO convertToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .permissions(user.getPermissions())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
