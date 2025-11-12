package com.conectaai.adapter.mercadopago;

import com.conectaai.adapter.gateway.GatewayRefundResponse;
import com.conectaai.adapter.gateway.RefundGatewayAdapter;
import com.conectaai.domain.Payment;
import com.conectaai.enums.Provider;
import com.conectaai.exception.GatewayException;
import com.conectaai.logger.AppLogger;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MercadoPagoRefundAdapter implements RefundGatewayAdapter {

    private static final AppLogger LOGGER = AppLogger.getLogger(MercadoPagoRefundAdapter.class);
    private static final String METHOD_CREATE_REFUND = "createRefund";

    @Value("${MP_ACCESS_TOKEN:}")
    private String mpAccessToken;

    @Override
    public GatewayRefundResponse createRefund(
            Payment payment,
            BigDecimal amount,
            String reason,
            String externalId) {
        LOGGER.info(METHOD_CREATE_REFUND, "Criando reembolso no Mercado Pago: payment={}, amount={}", 
                payment.getProviderPaymentId(), amount);

        try {
            MercadoPagoConfig.setAccessToken(mpAccessToken);
            PaymentClient client = new PaymentClient();
            
            Object refundResult = client.refund(Long.parseLong(payment.getProviderPaymentId()), amount);
            Object refundedPayment = refundResult;

            Map<String, Object> metadata = new HashMap<>();
            String status = extractRefundStatus(refundedPayment);
            Object id = extractRefundId(refundedPayment);
            metadata.put("status", status);

            return new GatewayRefundResponse(
                    id != null ? id.toString() : String.valueOf(System.currentTimeMillis()),
                    status,
                    amount,
                    java.time.OffsetDateTime.now(),
                    metadata
            );
        } catch (MPException | MPApiException e) {
            LOGGER.error("createRefund", "Erro ao criar reembolso no Mercado Pago", e);
            throw new GatewayException(Provider.MERCADO_PAGO, "Erro ao criar reembolso: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewayRefundResponse getRefund(String providerRefundId) {
        LOGGER.info("getRefund", "Buscando reembolso no Mercado Pago: id={}", providerRefundId);

        try {
            MercadoPagoConfig.setAccessToken(mpAccessToken);
            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment payment = client.get(Long.parseLong(providerRefundId));

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("status", payment.getStatus());

            return new GatewayRefundResponse(
                    payment.getId().toString(),
                    payment.getStatus(),
                    payment.getTransactionAmount(),
                    null,
                    metadata
            );
        } catch (MPException | MPApiException e) {
            LOGGER.error("getRefund", "Erro ao buscar reembolso no Mercado Pago", e);
            throw new GatewayException(Provider.MERCADO_PAGO, "Erro ao buscar reembolso: " + e.getMessage(), e);
        }
    }

    private String extractRefundStatus(Object refundedPayment) {
        try {
            return (String) refundedPayment.getClass().getMethod("getStatus").invoke(refundedPayment);
        } catch (Exception e) {
            LOGGER.warn("extractRefundStatus", "Erro ao extrair status via reflection: {}", e.getMessage());
            return "refunded";
        }
    }

    private Object extractRefundId(Object refundedPayment) {
        try {
            return refundedPayment.getClass().getMethod("getId").invoke(refundedPayment);
        } catch (Exception e) {
            LOGGER.warn("extractRefundId", "Erro ao extrair id via reflection: {}", e.getMessage());
            return extractRefundPaymentId(refundedPayment);
        }
    }

    private Object extractRefundPaymentId(Object refundedPayment) {
        try {
            return refundedPayment.getClass().getMethod("getPaymentId").invoke(refundedPayment);
        } catch (Exception e) {
            LOGGER.warn("extractRefundPaymentId", "Erro ao extrair paymentId: {}", e.getMessage());
            return null;
        }
    }
}

