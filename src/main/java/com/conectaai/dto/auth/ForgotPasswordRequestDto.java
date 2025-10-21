package com.conectaai.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDto(
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email
) { }

