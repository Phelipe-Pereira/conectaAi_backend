package com.conectaai.dto.refund;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RefundRequestDto(
        @NotNull(message = "O ID do pagamento é obrigatório")
        @JsonProperty("payment_id")
        Long paymentId,

        @NotNull(message = "O valor do reembolso é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor mínimo do reembolso é R$ 0,01")
        BigDecimal amount,

        @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres")
        String reason
) {
}
