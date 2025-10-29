package com.conectaai.controller.customer;

import com.conectaai.config.PaginationConfig;
import com.conectaai.dto.customer.CustomerRequestDto;
import com.conectaai.dto.customer.CustomerResponseDto;
import com.conectaai.dto.customer.CustomerUpdateDto;
import com.conectaai.service.customer.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Validated
public class CustomerController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "firstName", "lastName", "email", "createdAt", "updatedAt", "active", "city", "state"
    );

    private final CustomerService customerService;
    private final PaginationConfig paginationConfig;

    @PostMapping
    public ResponseEntity<CustomerResponseDto> createCustomer(@Valid @RequestBody CustomerRequestDto customerRequest) {
        CustomerResponseDto createdCustomer = customerService.createCustomer(customerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable Long id) {
        CustomerResponseDto customer = customerService.findById(id);
        return ResponseEntity.ok(customer);
    }

    @GetMapping
    public ResponseEntity<Page<CustomerResponseDto>> findCustomers(
            @RequestParam(required = false) String externalId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {

        Pageable pageable = createPageable(page, size, sort, direction);
        Page<CustomerResponseDto> customers = customerService.findCustomers(
                externalId, active, term, city, state, pageable);
        
        return ResponseEntity.ok(customers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateDto updateRequest) {
        CustomerResponseDto updatedCustomer = customerService.updateCustomer(id, updateRequest);
        return ResponseEntity.ok(updatedCustomer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateCustomer(@PathVariable Long id) {
        customerService.toggleCustomerStatus(id, false);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateCustomer(@PathVariable Long id) {
        customerService.toggleCustomerStatus(id, true);
        return ResponseEntity.noContent().build();
    }

    private Pageable createPageable(int page, Integer size, String sort, String direction) {
        int maxSize = paginationConfig.getMaxPageSize();
        int pageSize = size != null ? Math.min(size, maxSize) : paginationConfig.getDefaultPageSize();
        
        String validatedSort = ALLOWED_SORT_FIELDS.contains(sort) ? sort : "createdAt";
        Sort.Direction sortDirection = parseDirection(direction);
        
        return PageRequest.of(page, pageSize, Sort.by(sortDirection, validatedSort));
    }

    private Sort.Direction parseDirection(String direction) {
        try {
            return Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException e) {
            return Sort.Direction.DESC;
        }
    }
}
