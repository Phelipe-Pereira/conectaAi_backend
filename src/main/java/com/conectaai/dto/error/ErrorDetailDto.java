package com.conectaai.dto.error;

import java.util.List;

public record ErrorDetailDto(
        String code,
        String message,
        List<ValidationErrorDto> details
) {
    public ErrorDetailDto(String code, String message) {
        this(code, message, null);
    }
}

