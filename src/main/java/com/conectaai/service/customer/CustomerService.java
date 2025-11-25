package com.conectaai.service.customer;

import com.conectaai.adapter.factory.CustomerGatewayAdapterFactory;
import com.conectaai.domain.Customer;
import com.conectaai.dto.customer.CustomerMapper;
import com.conectaai.dto.customer.CustomerRequestDto;
import com.conectaai.dto.customer.CustomerResponseDto;
import com.conectaai.dto.customer.CustomerUpdateDto;
import com.conectaai.enums.Provider;
import com.conectaai.exception.CustomerAlreadyExistsException;
import com.conectaai.exception.CustomerNotFoundException;
import com.conectaai.logger.AppLogger;
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

    private static final AppLogger LOGGER = AppLogger.getLogger(CustomerService.class);
    private static final String METHOD_CREATE_CUSTOMER = "createCustomer";

    private final CustomerRepository customerRepository;
    private final CustomerGatewayAdapterFactory customerAdapterFactory;
    private final com.conectaai.repository.UserRepository userRepository;

    @Transactional
    public CustomerResponseDto createCustomer(CustomerRequestDto customerRequest) {
        validateDocumentPresence(customerRequest.cpf(), customerRequest.cnpj());
        validateUniqueness(customerRequest.email(), customerRequest.cpf(), customerRequest.cnpj(), null);

        Long currentUserId = com.conectaai.security.SecurityUtils.getCurrentUserId();
        com.conectaai.domain.User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new com.conectaai.exception.UserNotFoundException("Usuário não encontrado"));

        Customer customer = CustomerMapper.toEntity(customerRequest);
        customer.setUser(currentUser);
        Customer savedCustomer = customerRepository.save(customer);
        LOGGER.info(METHOD_CREATE_CUSTOMER, "Customer salvo localmente com ID: {}", savedCustomer.getId());
        
        try {
            LOGGER.info(METHOD_CREATE_CUSTOMER, "Tentando criar customer no gateway: {}", customerRequest.provider());
            createCustomerInGateway(savedCustomer, customerRequest.provider());
            LOGGER.info(METHOD_CREATE_CUSTOMER, "Criação no gateway concluída com sucesso");
        } catch (Exception e) {
            LOGGER.error(METHOD_CREATE_CUSTOMER, 
                    "Erro ao criar customer no gateway {}: {} - Exception: {}", 
                    customerRequest.provider(), e.getMessage(), e.getClass().getName(), e);
        }
        
        Customer updatedCustomer = customerRepository.findById(savedCustomer.getId())
                .orElse(savedCustomer);
        
        return CustomerMapper.toResponseDto(updatedCustomer);
    }

    private void createCustomerInGateway(Customer customer, Provider provider) {
        LOGGER.info(METHOD_CREATE_CUSTOMER, "Iniciando criação no gateway: provider={}, customerId={}", 
                provider, customer.getId());
        try {
            LOGGER.info(METHOD_CREATE_CUSTOMER, "Obtendo adapter para provider: {}", provider);
            var adapter = customerAdapterFactory.getAdapter(provider);
            LOGGER.info(METHOD_CREATE_CUSTOMER, "Adapter obtido com sucesso, chamando createCustomer");
            String providerCustomerId = adapter.createCustomer(customer);
            LOGGER.info(METHOD_CREATE_CUSTOMER, "ProviderCustomerId recebido: {}", providerCustomerId);
            customer.setProviderCustomerId(providerCustomerId);
            customerRepository.save(customer);
            LOGGER.info(METHOD_CREATE_CUSTOMER, 
                    "Customer criado no gateway {} com sucesso: providerCustomerId={}", 
                    provider, providerCustomerId);
        } catch (Exception e) {
            LOGGER.error(METHOD_CREATE_CUSTOMER, 
                    "Erro ao criar customer no gateway {}: {} - StackTrace: {}", 
                    provider, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponseDto> findCustomers(
            String externalId,
            Boolean active,
            String term,
            String city,
            String state,
            Pageable pageable) {

        Long currentUserId = com.conectaai.security.SecurityUtils.getCurrentUserId();
        Specification<Customer> spec = CustomerSpecification.hasUserId(currentUserId)
                .and(buildSpecification(externalId, active, term, city, state));
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

    @Transactional
    public void updateCustomerProviderId(Customer customer) {
        customerRepository.save(customer);
    }

    @Transactional
    public void ensureCustomerInGateway(Customer customer, Provider provider) {
        if (com.conectaai.utils.CustomerGatewayUtils.needsGatewayCreation(customer, provider)) {
            try {
                var adapter = customerAdapterFactory.getAdapter(Provider.ASAAS);
                String providerCustomerId = adapter.createCustomer(customer);
                customer.setProviderCustomerId(providerCustomerId);
                customerRepository.save(customer);
                LOGGER.info("ensureCustomerInGateway", 
                        "Customer criado no gateway {} com sucesso: providerCustomerId={}", 
                        provider, providerCustomerId);
            } catch (Exception e) {
                LOGGER.warn("ensureCustomerInGateway", 
                        "Erro ao criar customer no gateway {}: {}", provider, e.getMessage());
            }
        }
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
