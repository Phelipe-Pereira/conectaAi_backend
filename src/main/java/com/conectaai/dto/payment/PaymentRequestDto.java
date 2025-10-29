package com.conectaai.dto.payment;

import com.conectaai.enums.Currency;
import com.conectaai.enums.Provider;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequestDto(

        @NotNull(message = "ID do cliente é obrigatório")
        @JsonProperty("customer_id")
        Long customerId,

        @NotNull(message = "Provedor é obrigatório")
        Provider provider,

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "5.00", message = "Valor mínimo é R$ 5,00")
        @Digits(integer = 17, fraction = 2, message = "Valor deve ter no máximo 2 casas decimais")
        BigDecimal amount,

        @NotNull(message = "Moeda é obrigatória")
        Currency currency,

        @NotBlank(message = "Método de pagamento é obrigatório")
        @Size(max = 50, message = "Método de pagamento deve ter no máximo 50 caracteres")
        @JsonProperty("payment_method")
        String paymentMethod,

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        String description,

        @NotNull(message = "Data de vencimento é obrigatória")
        @Future(message = "Data de vencimento deve ser no futuro")
        @JsonProperty("due_date")
        LocalDate dueDate
) {
}
