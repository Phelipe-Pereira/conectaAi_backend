package com.conectaai.dto.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CustomerUpdateDto(
        @Size(min = 2, max = 100, message = "Primeiro nome deve ter entre 2 e 100 caracteres")
        @JsonProperty("first_name")
        String firstName,
        
        @Size(min = 2, max = 100, message = "Sobrenome deve ter entre 2 e 100 caracteres")
        @JsonProperty("last_name")
        String lastName,
        
        @Email(message = "Email inválido")
        @Size(max = 254, message = "Email muito longo")
        String email,
        
        @Pattern(regexp = "^\\d{10,11}$", message = "Telefone deve ter 10 ou 11 dígitos")
        String phone,
        
        @Size(max = 255, message = "Endereço muito longo")
        String address,
        
        @Positive(message = "Número do endereço deve ser positivo")
        @JsonProperty("address_number")
        Integer addressNumber,
        
        String complement,
        
        @Size(max = 100, message = "Nome da cidade muito longo")
        String city,
        
        @Pattern(regexp = "^[A-Z]{2}$", message = "Estado deve ter 2 letras maiúsculas")
        String state,
        
        @Pattern(regexp = "^\\d{8}$", message = "CEP deve ter 8 dígitos")
        @JsonProperty("zip_code")
        String zipCode,
        
        @Pattern(regexp = "^[A-Z]{2}$", message = "País deve ter 2 letras maiúsculas")
        String country,
        
        @Past(message = "Data de nascimento deve ser no passado")
        @JsonProperty("birth_date")
        LocalDate birthDate,
        
        @Size(max = 100, message = "Nome da empresa muito longo")
        @JsonProperty("company_name")
        String companyName
) {
}

