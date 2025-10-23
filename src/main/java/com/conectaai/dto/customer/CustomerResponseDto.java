package com.conectaai.dto.customer;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CustomerResponseDto(
        String id,
        
        @JsonProperty("first_name")
        String firstName,
        
        @JsonProperty("last_name")
        String lastName,
        
        @JsonProperty("full_name")
        String fullName,
        
        String email,
        
        @JsonProperty("cpf_masked")
        String cpfMasked,
        
        @JsonProperty("cnpj_masked")
        String cnpjMasked,
        
        String phone,
        
        String address,
        
        @JsonProperty("address_number")
        Integer addressNumber,
        
        String complement,
        
        String city,
        
        String state,
        
        @JsonProperty("zip_code")
        String zipCode,
        
        String country,
        
        @JsonProperty("birth_date")
        LocalDate birthDate,
        
        @JsonProperty("company_name")
        String companyName,
        
        Boolean active,
        
        @JsonProperty("created_at")
        LocalDateTime createdAt,
        
        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {
}

