package com.conectaai.dto.apikey;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApiKeyRequestDto(
        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 1, max = 100, message = "Nome deve ter entre 1 e 100 caracteres")
        String name,

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        String description
) {
}

