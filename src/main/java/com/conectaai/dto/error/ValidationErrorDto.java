package com.conectaai.dto.error;

public record ValidationErrorDto(
        String field,
        String message
) { }

