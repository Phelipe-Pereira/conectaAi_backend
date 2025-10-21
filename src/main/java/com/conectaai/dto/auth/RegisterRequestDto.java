package com.conectaai.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(

        @NotBlank(message = "Username é obrigatório")
        @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]+$",
                message = "Username deve conter apenas letras, números, ponto, underscore ou hífen"
        )
        String username,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 255, message = "Email não pode ter mais de 255 caracteres")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Senha deve conter: letra maiúscula, minúscula, número e caractere especial"
        )
        String password

) { }