package com.conectaai.adapter.stripe;

import com.conectaai.adapter.gateway.GatewayRefundResponse;
import com.conectaai.adapter.gateway.RefundGatewayAdapter;
import com.conectaai.domain.Payment;
import com.conectaai.enums.Provider;
import com.conectaai.exception.GatewayException;
import com.conectaai.logger.AppLogger;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StripeRefundAdapter implements RefundGatewayAdapter {

    private static final AppLogger LOGGER = AppLogger.getLogger(StripeRefundAdapter.class);

    @Override
    public GatewayRefundResponse createRefund(
            Payment payment,
            BigDecimal amount,
            String reason,
            String externalId) {
        LOGGER.info("createRefund", "Criando reembolso no Stripe: payment={}, amount={}", 
                payment.getProviderPaymentId(), amount);

        try {
            long amountInCents = amount.multiply(new BigDecimal("100")).longValue();
            
            RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getProviderPaymentId())
                    .setAmount(amountInCents);

            if (reason != null && !reason.isBlank()) {
                paramsBuilder.setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);
            }

            Map<String, String> metadata = new HashMap<>();
            metadata.put("external_id", externalId);
            paramsBuilder.putAllMetadata(metadata);

            RefundCreateParams params = paramsBuilder.build();
            Refund refund = Refund.create(params);

            Map<String, Object> responseMetadata = new HashMap<>();
            responseMetadata.put("status", refund.getStatus());

            return new GatewayRefundResponse(
                    refund.getId(),
                    refund.getStatus(),
                    amount,
                    refund.getCreated() != null 
                            ? OffsetDateTime.ofInstant(Instant.ofEpochSecond(refund.getCreated()), 
                                    ZoneId.systemDefault())
                            : null,
                    responseMetadata
            );
        } catch (StripeException e) {
            LOGGER.error("createRefund", "Erro ao criar reembolso no Stripe", e);
            throw new GatewayException(Provider.STRIPE, "Erro ao criar reembolso: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewayRefundResponse getRefund(String providerRefundId) {
        LOGGER.info("getRefund", "Buscando reembolso no Stripe: id={}", providerRefundId);

        try {
            Refund refund = Refund.retrieve(providerRefundId);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("status", refund.getStatus());

            return new GatewayRefundResponse(
                    refund.getId(),
                    refund.getStatus(),
                    BigDecimal.valueOf(refund.getAmount()).divide(new BigDecimal("100")),
                    refund.getCreated() != null 
                            ? OffsetDateTime.ofInstant(Instant.ofEpochSecond(refund.getCreated()), 
                                    ZoneId.systemDefault())
                            : null,
                    metadata
            );
        } catch (StripeException e) {
            LOGGER.error("getRefund", "Erro ao buscar reembolso no Stripe", e);
            throw new GatewayException(Provider.STRIPE, "Erro ao buscar reembolso: " + e.getMessage(), e);
        }
    }
}

