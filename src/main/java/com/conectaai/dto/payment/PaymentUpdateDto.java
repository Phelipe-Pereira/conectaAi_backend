package com.conectaai.dto.payment;

import com.conectaai.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record PaymentUpdateDto(

        PaymentStatus status,

        @Size(max = 100, message = "ID do provedor deve ter no máximo 100 caracteres")
        @JsonProperty("provider_payment_id")
        String providerPaymentId,

        @JsonProperty("paid_at")
        LocalDateTime paidAt,

        @Size(max = 500, message = "URL de pagamento deve ter no máximo 500 caracteres")
        @JsonProperty("payment_url")
        String paymentUrl,

        @JsonProperty("qr_code")
        String qrCode,

        @Size(max = 100, message = "Código de barras deve ter no máximo 100 caracteres")
        @JsonProperty("bar_code")
        String barCode
) {
}

