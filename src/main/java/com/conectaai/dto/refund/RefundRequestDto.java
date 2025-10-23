package com.conectaai.dto.refund;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RefundRequestDto(
        @NotBlank(message = "ID do pagamento é obrigatório")
        @JsonProperty("payment_id")
        String paymentId,
        
        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor mínimo é 0.01")
        @Digits(integer = 17, fraction = 2, message = "Valor inválido")
        BigDecimal amount,
        
        @Size(max = 500, message = "Motivo muito longo")
        String reason
) {
}

