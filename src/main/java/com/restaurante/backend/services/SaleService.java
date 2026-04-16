package com.restaurante.backend.services;

import com.restaurante.backend.dto.ItemSaleRequestDTO;
import com.restaurante.backend.dto.SaleRequestDTO;
import com.restaurante.backend.dto.SaleResponseDTO;
import com.restaurante.backend.entities.Customer;
import com.restaurante.backend.entities.ItemSale;
import com.restaurante.backend.entities.Product;
import com.restaurante.backend.entities.Sale;
import com.restaurante.backend.entities.enums.DeliveryType;
import com.restaurante.backend.entities.enums.SaleStatus;
import com.restaurante.backend.mapper.SaleMapper;
import com.restaurante.backend.repositories.CustomerRepository;
import com.restaurante.backend.repositories.ItemSaleRepository;
import com.restaurante.backend.repositories.ProductRepository;
import com.restaurante.backend.repositories.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ItemSaleRepository itemSaleRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SaleMapper saleMapper;

    public SaleService(SaleRepository saleRepository, ItemSaleRepository itemSaleRepository,
                       CustomerRepository customerRepository, ProductRepository productRepository,
                       SaleMapper saleMapper) {
        this.saleRepository = saleRepository;
        this.itemSaleRepository = itemSaleRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.saleMapper = saleMapper;
    }

    @Transactional
    public SaleResponseDTO create(SaleRequestDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("A venda deve conter ao menos 1 item.");
        }

        Customer customer;
        if (dto.getCustomerPhone() != null && !dto.getCustomerPhone().isBlank()) {
            java.util.Optional<Customer> existingCustomer = customerRepository.findByPhone(dto.getCustomerPhone());
            
            if (existingCustomer.isPresent()) {
                customer = existingCustomer.get();
                boolean updated = false;
                
                // Se o nome for diferente mas o telefone o mesmo. Deverá atualizar o nome novo no registro existente.
                if (dto.getCustomerName() != null && !dto.getCustomerName().isBlank() 
                        && !dto.getCustomerName().equalsIgnoreCase(customer.getName())) {
                    customer.setName(dto.getCustomerName());
                    updated = true;
                }
                
                // Se o endereço for diferente mas o telefone o mesmo. Deverá salvar o novo endereço no cliente existente.
                String newAddrStr = dto.getCustomerAddress();
                if (newAddrStr != null && !newAddrStr.isBlank()) {
                    if (customer.getAddress() == null) {
                        com.restaurante.backend.entities.Address addr = new com.restaurante.backend.entities.Address();
                        addr.setLogradouro(newAddrStr);
                        customer.setAddress(addr);
                        updated = true;
                    } else if (!newAddrStr.equalsIgnoreCase(customer.getAddress().getLogradouro())) {
                        customer.getAddress().setLogradouro(newAddrStr);
                        updated = true;
                    }
                }
                
                if (updated) {
                    customer = customerRepository.save(customer);
                }
            } else {
                // O cliente identificado deverá ser salvo no banco de dados se o telefone não for encontrado.
                customer = new Customer();
                customer.setName(dto.getCustomerName());
                customer.setPhone(dto.getCustomerPhone());
                
                com.restaurante.backend.entities.Address addr = new com.restaurante.backend.entities.Address();
                addr.setLogradouro(dto.getCustomerAddress());
                customer.setAddress(addr);
                
                customer = customerRepository.save(customer);
            }
        } else if (dto.getCustomerId() != null) {
            customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found with id " + dto.getCustomerId()));
        } else {
            throw new IllegalArgumentException("Cliente não informado (Telefone ou ID necessário).");
        }

        Sale sale = new Sale();
        sale.setDiscount(dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO);
        sale.setCustomer(customer);
        
        if (dto.getDeliveryType() != null) {
            try {
                sale.setDeliveryType(DeliveryType.valueOf(dto.getDeliveryType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                sale.setDeliveryType(DeliveryType.ENTREGA);
            }
        } else {
            sale.setDeliveryType(DeliveryType.ENTREGA);
        }
        
        sale.setStatus(SaleStatus.EM_APROVACAO);

        List<ItemSale> items = dto.getItems().stream().map(itemDto -> {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with id " + itemDto.getProductId()));
            
            ItemSale itemSale = new ItemSale();
            itemSale.setProduct(product);
            itemSale.setSale(sale);
            itemSale.setQuantity(itemDto.getQuantity());
            itemSale.setUnitValue(product.getSalePrice());
            itemSale.setDiscount(itemDto.getDiscount() != null ? itemDto.getDiscount() : BigDecimal.ZERO);
            
            // Calc Total = (Qty * Unit) - Discount
            BigDecimal qtyBigDecimal = new BigDecimal(itemSale.getQuantity());
            BigDecimal itemTotal = itemSale.getUnitValue().multiply(qtyBigDecimal).subtract(itemSale.getDiscount());
            itemSale.setTotalValue(itemTotal);
            
            return itemSale;
        }).collect(Collectors.toList());

        sale.setItems(items);
        recalculateSaleTotal(sale);

        Sale savedSale = saleRepository.save(sale);
        return saleMapper.toResponse(savedSale);
    }

    @Transactional(readOnly = true)
    public List<SaleResponseDTO> findAll() {
        return saleRepository.findAll().stream()
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SaleResponseDTO findById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found with id " + id));
        return saleMapper.toResponse(sale);
    }

    @Transactional
    public void delete(Long id) {
        if (!saleRepository.existsById(id)) {
            throw new IllegalArgumentException("Sale not found with id " + id);
        }
        saleRepository.deleteById(id);
    }

    @Transactional
    public SaleResponseDTO updateStatus(Long id, String statusStr) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found with id " + id));
        
        SaleStatus currentStatus = sale.getStatus();
        SaleStatus newStatus;
        try {
            newStatus = SaleStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + statusStr);
        }

        // --- Role-based Permission Logic ---
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        boolean isMaster = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MASTER"));
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (!isMaster && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("Usuário sem permissão para alterar status.");
        }

        // Logic: Forward (Admin/Master), Backward (Master Only), Cancel (Admin/Master)
        boolean isForward = newStatus.ordinal() > currentStatus.ordinal();
        boolean isBackward = newStatus.ordinal() < currentStatus.ordinal();
        boolean involvesCancel = newStatus == SaleStatus.CANCELADA || currentStatus == SaleStatus.CANCELADA;

        if (isBackward && !involvesCancel && !isMaster) {
            throw new org.springframework.security.access.AccessDeniedException("Apenas usuários MASTER podem retornar pedidos no fluxo.");
        }

        sale.setStatus(newStatus);
        Sale savedSale = saleRepository.save(sale);
        return saleMapper.toResponse(savedSale);
    }

    @Transactional
    public SaleResponseDTO addItemToSale(Long saleId, ItemSaleRequestDTO dto) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found with id " + saleId));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id " + dto.getProductId()));

        ItemSale itemSale = new ItemSale();
        itemSale.setProduct(product);
        itemSale.setSale(sale);
        itemSale.setQuantity(dto.getQuantity());
        itemSale.setUnitValue(product.getSalePrice());
        itemSale.setDiscount(dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO);

        BigDecimal qtyBigDecimal = new BigDecimal(itemSale.getQuantity());
        BigDecimal itemTotal = itemSale.getUnitValue().multiply(qtyBigDecimal).subtract(itemSale.getDiscount());
        itemSale.setTotalValue(itemTotal);

        sale.getItems().add(itemSale);
        recalculateSaleTotal(sale);

        Sale savedSale = saleRepository.save(sale);
        return saleMapper.toResponse(savedSale);
    }

    @Transactional
    public SaleResponseDTO updateItemInSale(Long itemId, ItemSaleRequestDTO dto) {
        ItemSale itemSale = itemSaleRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("ItemSale not found with id " + itemId));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id " + dto.getProductId()));

        itemSale.setProduct(product);
        itemSale.setQuantity(dto.getQuantity());
        itemSale.setUnitValue(product.getSalePrice());
        itemSale.setDiscount(dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO);

        BigDecimal qtyBigDecimal = new BigDecimal(itemSale.getQuantity());
        BigDecimal itemTotal = itemSale.getUnitValue().multiply(qtyBigDecimal).subtract(itemSale.getDiscount());
        itemSale.setTotalValue(itemTotal);

        Sale sale = itemSale.getSale();
        recalculateSaleTotal(sale);

        Sale savedSale = saleRepository.save(sale);
        return saleMapper.toResponse(savedSale);
    }

    @Transactional
    public SaleResponseDTO removeItemFromSale(Long saleId, Long itemId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found with id " + saleId));

        if (sale.getItems().size() <= 1) {
            throw new IllegalArgumentException("A venda deve conter ao menos 1 item.");
        }

        ItemSale itemToRemove = sale.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("ItemSale not found in this sale."));

        sale.getItems().remove(itemToRemove);
        recalculateSaleTotal(sale);

        Sale savedSale = saleRepository.save(sale);
        return saleMapper.toResponse(savedSale);
    }

    private void recalculateSaleTotal(Sale sale) {
        BigDecimal totalItems = sale.getItems().stream()
                .map(ItemSale::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal saleDiscount = sale.getDiscount() != null ? sale.getDiscount() : BigDecimal.ZERO;
        sale.setTotalValue(totalItems.subtract(saleDiscount));
    }
}
