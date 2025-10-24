package com.conectaai.dto.payment;

import com.conectaai.enums.Currency;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentRequestDto(
        @NotBlank(message = "ID do cliente é obrigatório")
        @JsonProperty("customer_id")
        String customerId,
        
        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor mínimo é 0.01")
        @Digits(integer = 17, fraction = 2, message = "Valor inválido")
        BigDecimal amount,
        
        @NotNull(message = "Moeda é obrigatória")
        Currency currency,
        
        @Size(max = 500, message = "Descrição muito longa")
        String description,
        
        @NotBlank(message = "Método de pagamento é obrigatório")
        @Size(max = 50, message = "Método de pagamento inválido")
        @JsonProperty("payment_method")
        String paymentMethod,
        
        @JsonProperty("due_date")
        LocalDateTime dueDate,
        
        @JsonProperty("installments")
        @Min(value = 1, message = "Número de parcelas deve ser no mínimo 1")
        @Max(value = 12, message = "Número de parcelas deve ser no máximo 12")
        Integer installments
) {
}

