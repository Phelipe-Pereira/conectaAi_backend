package com.conectaai.dto.subscription;

import com.conectaai.enums.Currency;
import com.conectaai.enums.Provider;
import com.conectaai.enums.SubscriptionInterval;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionRequestDto(
        @NotNull(message = "Customer ID é obrigatório")
        @JsonProperty("customer_id")
        Long customerId,

        @NotNull(message = "Provider é obrigatório")
        Provider provider,

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "5.00", message = "Valor mínimo é R$ 5,00")
        @Digits(integer = 17, fraction = 2, message = "Valor inválido")
        BigDecimal amount,

        @NotNull(message = "Moeda é obrigatória")
        Currency currency,

        @NotNull(message = "Intervalo é obrigatório")
        SubscriptionInterval interval,

        @NotNull(message = "Método de pagamento é obrigatório")
        @NotBlank(message = "Método de pagamento não pode ser vazio")
        @Size(max = 50)
        @JsonProperty("payment_method")
        String paymentMethod,

        @Size(max = 500)
        String description,

        @NotNull(message = "Data de início é obrigatória")
        @Future(message = "Data de início deve estar no futuro")
        @JsonProperty("start_at")
        LocalDateTime startAt,

        @JsonProperty("end_at")
        LocalDateTime endAt
) {
}
