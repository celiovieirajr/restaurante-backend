package com.restaurante.backend.controllers;

import com.restaurante.backend.dto.ItemSaleRequestDTO;
import com.restaurante.backend.dto.SaleRequestDTO;
import com.restaurante.backend.dto.SaleResponseDTO;
import com.restaurante.backend.services.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    public ResponseEntity<SaleResponseDTO> create(@Valid @RequestBody SaleRequestDTO dto) {
        try {
            SaleResponseDTO response = saleService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_MASTER', 'VENDA_VIEW')")
    public ResponseEntity<List<SaleResponseDTO>> findAll() {
        return ResponseEntity.ok(saleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.findById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_MASTER', 'VENDA_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        saleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_MASTER', 'VENDA_EDIT')")
    public ResponseEntity<SaleResponseDTO> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(saleService.updateStatus(id, status));
    }

    @PostMapping("/{saleId}/items")
    @PreAuthorize("hasAnyAuthority('ROLE_MASTER', 'VENDA_EDIT')")
    public ResponseEntity<SaleResponseDTO> addItemToSale(@PathVariable Long saleId,
            @Valid @RequestBody ItemSaleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saleService.addItemToSale(saleId, dto));
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasAnyAuthority('ROLE_MASTER', 'VENDA_EDIT')")
    public ResponseEntity<SaleResponseDTO> updateItemInSale(@PathVariable Long itemId,
            @Valid @RequestBody ItemSaleRequestDTO dto) {
        return ResponseEntity.ok(saleService.updateItemInSale(itemId, dto));
    }

    @DeleteMapping("/{saleId}/items/{itemId}")
    @PreAuthorize("hasAnyAuthority('ROLE_MASTER', 'VENDA_EDIT')")
    public ResponseEntity<SaleResponseDTO> removeItemFromSale(@PathVariable Long saleId, @PathVariable Long itemId) {
        return ResponseEntity.ok(saleService.removeItemFromSale(saleId, itemId));
    }
}
