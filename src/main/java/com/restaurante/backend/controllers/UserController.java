package com.restaurante.backend.controllers;

import com.restaurante.backend.dto.PasswordUpdateRequestDTO;
import com.restaurante.backend.dto.UserRequestDTO;
import com.restaurante.backend.dto.UserResponseDTO;
import com.restaurante.backend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id, @Valid @RequestBody PasswordUpdateRequestDTO dto) {
        userService.updatePassword(id, dto.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/permissions")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<UserResponseDTO> updatePermissions(@PathVariable Long id, @RequestBody java.util.Set<String> permissions) {
        return ResponseEntity.ok(userService.updatePermissions(id, permissions));
    }
}
