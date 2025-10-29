package com.conectaai.dto.customer;

import com.conectaai.domain.Customer;

import java.util.UUID;
public class CustomerMapper {

    private CustomerMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Customer toEntity(CustomerRequestDto customerRequest) {
        return Customer.builder()
                .externalId(UUID.randomUUID().toString())
                .firstName(customerRequest.firstName())
                .lastName(customerRequest.lastName())
                .email(customerRequest.email())
                .cpf(customerRequest.cpf())
                .cnpj(customerRequest.cnpj())
                .phone(customerRequest.phone())
                .address(customerRequest.address())
                .addressNumber(customerRequest.addressNumber())
                .complement(customerRequest.complement())
                .city(customerRequest.city())
                .state(customerRequest.state())
                .zipCode(customerRequest.zipCode())
                .country(customerRequest.country() != null ? customerRequest.country() : "BR")
                .birthDate(customerRequest.birthDate())
                .companyName(customerRequest.companyName())
                .active(true)
                .build();
    }

    public static CustomerResponseDto toResponseDto(Customer customer) {
        return new CustomerResponseDto(
                customer.getId().toString(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getFirstName() + " " + customer.getLastName(),
                customer.getEmail(),
                customer.getCpf(),
                customer.getCnpj(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getAddressNumber(),
                customer.getComplement(),
                customer.getCity(),
                customer.getState(),
                customer.getZipCode(),
                customer.getCountry(),
                customer.getBirthDate(),
                customer.getCompanyName(),
                customer.getActive(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public static void updateEntity(Customer customer, CustomerUpdateDto updateRequest) {
        if (updateRequest.firstName() != null) {
            customer.setFirstName(updateRequest.firstName());
        }
        if (updateRequest.lastName() != null) {
            customer.setLastName(updateRequest.lastName());
        }
        if (updateRequest.email() != null) {
            customer.setEmail(updateRequest.email());
        }
        if (updateRequest.cpf() != null) {
            customer.setCpf(updateRequest.cpf());
            customer.setCnpj(null);
        }
        if (updateRequest.cnpj() != null) {
            customer.setCnpj(updateRequest.cnpj());
            customer.setCpf(null);
        }
        if (updateRequest.phone() != null) {
            customer.setPhone(updateRequest.phone());
        }
        if (updateRequest.address() != null) {
            customer.setAddress(updateRequest.address());
        }
        if (updateRequest.addressNumber() != null) {
            customer.setAddressNumber(updateRequest.addressNumber());
        }
        if (updateRequest.complement() != null) {
            customer.setComplement(updateRequest.complement());
        }
        if (updateRequest.city() != null) {
            customer.setCity(updateRequest.city());
        }
        if (updateRequest.state() != null) {
            customer.setState(updateRequest.state());
        }
        if (updateRequest.zipCode() != null) {
            customer.setZipCode(updateRequest.zipCode());
        }
        if (updateRequest.country() != null) {
            customer.setCountry(updateRequest.country());
        }
        if (updateRequest.birthDate() != null) {
            customer.setBirthDate(updateRequest.birthDate());
        }
        if (updateRequest.companyName() != null) {
            customer.setCompanyName(updateRequest.companyName());
        }
    }

}

