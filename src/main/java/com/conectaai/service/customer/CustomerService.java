package com.conectaai.service.customer;

import com.conectaai.domain.Customer;
import com.conectaai.dto.customer.CustomerMapper;
import com.conectaai.dto.customer.CustomerRequestDto;
import com.conectaai.dto.customer.CustomerResponseDto;
import com.conectaai.dto.customer.CustomerUpdateDto;
import com.conectaai.exception.CustomerAlreadyExistsException;
import com.conectaai.exception.CustomerNotFoundException;
import com.conectaai.repository.CustomerRepository;
import com.conectaai.specification.CustomerSpecification;
import com.conectaai.utils.DataNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponseDto createCustomer(CustomerRequestDto customerRequest) {
        validateDocumentPresence(customerRequest.cpf(), customerRequest.cnpj());
        validateUniqueness(customerRequest.email(), customerRequest.cpf(), customerRequest.cnpj(), null);

        Customer customer = CustomerMapper.toEntity(customerRequest);
        Customer savedCustomer = customerRepository.save(customer);
        return CustomerMapper.toResponseDto(savedCustomer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponseDto> findCustomers(
            String externalId,
            Boolean active,
            String term,
            String city,
            String state,
            Pageable pageable) {

        Specification<Customer> spec = buildSpecification(externalId, active, term, city, state);
        return customerRepository.findAll(spec, pageable)
                .map(CustomerMapper::toResponseDto);
    }

    @Transactional
    public CustomerResponseDto updateCustomer(Long id, CustomerUpdateDto updateRequest) {
        Customer customer = findByIdOrThrow(id);

        validateDocumentExclusivity(updateRequest.cpf(), updateRequest.cnpj());
        validateUniqueness(updateRequest.email(), updateRequest.cpf(), updateRequest.cnpj(), id);

        CustomerMapper.updateEntity(customer, updateRequest);
        Customer updatedCustomer = customerRepository.save(customer);
        return CustomerMapper.toResponseDto(updatedCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerResponseDto findById(Long id) {
        Customer customer = findByIdOrThrow(id);
        return CustomerMapper.toResponseDto(customer);
    }

    @Transactional
    public void toggleCustomerStatus(Long id, boolean active) {
        Customer customer = findByIdOrThrow(id);
        customer.setActive(active);
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer findCustomerEntityById(Long id) {
        return findByIdOrThrow(id);
    }

    private Customer findByIdOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Cliente não encontrado: " + id));
    }

    private Specification<Customer> buildSpecification(
            String externalId, 
            Boolean active, 
            String term, 
            String city, 
            String state) {
        
        return CustomerSpecification.hasExternalId(externalId)
                .and(CustomerSpecification.hasActive(active))
                .and(CustomerSpecification.searchByTerm(term))
                .and(CustomerSpecification.hasCity(city))
                .and(CustomerSpecification.hasState(state));
    }

    private void validateDocumentPresence(String cpf, String cnpj) {
        boolean hasCpf = cpf != null && !cpf.isBlank();
        boolean hasCnpj = cnpj != null && !cnpj.isBlank();

        if (!hasCpf && !hasCnpj) {
            throw new IllegalArgumentException("Cliente deve ter CPF ou CNPJ");
        }

        validateDocumentExclusivity(cpf, cnpj);
    }

    private void validateDocumentExclusivity(String cpf, String cnpj) {
        boolean hasCpf = cpf != null && !cpf.isBlank();
        boolean hasCnpj = cnpj != null && !cnpj.isBlank();

        if (hasCpf && hasCnpj) {
            throw new IllegalArgumentException("Cliente não pode ter CPF e CNPJ simultaneamente");
        }
    }

    private void validateUniqueness(String email, String cpf, String cnpj, Long excludeCustomerId) {
        if (email != null) {
            validateEmailUniqueness(email, excludeCustomerId);
        }
        if (cpf != null && !cpf.isBlank()) {
            validateCpfUniqueness(cpf, excludeCustomerId);
        }
        if (cnpj != null && !cnpj.isBlank()) {
            validateCnpjUniqueness(cnpj, excludeCustomerId);
        }
    }

    private void validateEmailUniqueness(String email, Long excludeCustomerId) {
        String normalizedEmail = DataNormalizer.email(email);
        if (normalizedEmail == null) {
            return;
        }

        if (excludeCustomerId == null) {
            if (customerRepository.existsByEmail(normalizedEmail)) {
                throw new CustomerAlreadyExistsException("Email já cadastrado: " + email);
            }
        } else {
            customerRepository.findByEmail(normalizedEmail).ifPresent(existingCustomer -> {
                if (!existingCustomer.getId().equals(excludeCustomerId)) {
                    throw new CustomerAlreadyExistsException("Email já cadastrado: " + email);
                }
            });
        }
    }

    private void validateCpfUniqueness(String cpf, Long excludeCustomerId) {
        String normalizedCpf = DataNormalizer.documentNumeric(cpf);
        if (normalizedCpf == null) {
            return;
        }

        if (excludeCustomerId == null) {
            if (customerRepository.existsByCpf(normalizedCpf)) {
                throw new CustomerAlreadyExistsException("CPF já cadastrado");
            }
        } else {
            customerRepository.findByCpf(normalizedCpf).ifPresent(existingCustomer -> {
                if (!existingCustomer.getId().equals(excludeCustomerId)) {
                    throw new CustomerAlreadyExistsException("CPF já cadastrado");
                }
            });
        }
    }

    private void validateCnpjUniqueness(String cnpj, Long excludeCustomerId) {
        String normalizedCnpj = DataNormalizer.documentNumeric(cnpj);
        if (normalizedCnpj == null) {
            return;
        }

        if (excludeCustomerId == null) {
            if (customerRepository.existsByCnpj(normalizedCnpj)) {
                throw new CustomerAlreadyExistsException("CNPJ já cadastrado");
            }
        } else {
            customerRepository.findByCnpj(normalizedCnpj).ifPresent(existingCustomer -> {
                if (!existingCustomer.getId().equals(excludeCustomerId)) {
                    throw new CustomerAlreadyExistsException("CNPJ já cadastrado");
                }
            });
        }
    }
}
