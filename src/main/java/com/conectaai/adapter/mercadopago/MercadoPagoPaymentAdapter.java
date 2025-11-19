package com.conectaai.adapter.mercadopago;

import com.conectaai.adapter.gateway.GatewayPaymentResponse;
import com.conectaai.adapter.gateway.PaymentGatewayAdapter;
import com.conectaai.domain.Customer;
import com.conectaai.enums.Provider;
import com.conectaai.exception.GatewayException;
import com.conectaai.logger.AppLogger;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MercadoPagoPaymentAdapter implements PaymentGatewayAdapter {

    private static final AppLogger LOGGER = AppLogger.getLogger(MercadoPagoPaymentAdapter.class);
    private static final String METHOD_CREATE_PAYMENT = "createPayment";
    private static final String METHOD_CANCEL_PAYMENT = "cancelPayment";

    @Value("${MP_ACCESS_TOKEN:}")
    private String mpAccessToken;

    @Override
    public GatewayPaymentResponse createPayment(
            Customer customer,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            String description,
            LocalDate dueDate,
            String externalId) {
        LOGGER.info(METHOD_CREATE_PAYMENT, "Criando pagamento no Mercado Pago: customer={}, amount={}", 
                customer.getExternalId(), amount);

        try {
            MercadoPagoConfig.setAccessToken(mpAccessToken);
            PaymentClient client = new PaymentClient();

            BigDecimal amountDecimal = amount;
            PaymentCreateRequest request;
            try {
                Object requestBuilder = PaymentCreateRequest.class.getMethod("builder").invoke(null);
                setTransactionAmount(requestBuilder, amountDecimal);
                String desc = description != null ? description : "Payment";
                setDescription(requestBuilder, desc);
                setExternalReference(requestBuilder, externalId);
                setCurrencyId(requestBuilder, currency);
                
                Object payer = buildPayer(customer);
                setPayer(requestBuilder, payer);
                
                request = (PaymentCreateRequest) requestBuilder.getClass()
                        .getMethod("build").invoke(requestBuilder);
            } catch (Exception e) {
                LOGGER.error(METHOD_CREATE_PAYMENT, "Erro ao criar request via reflection, usando fallback", e);
                String errorMsg = "Erro ao criar request de pagamento: " + e.getMessage();
                throw new GatewayException(Provider.MERCADO_PAGO, errorMsg, e);
            }

            Payment payment = client.create(request);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("status", payment.getStatus());
            metadata.put("status_detail", payment.getStatusDetail());

            return new GatewayPaymentResponse(
                    payment.getId().toString(),
                    payment.getStatus(),
                    amount,
                    currency,
                    extractTicketUrl(payment),
                    null,
                    null,
                    null,
                    metadata
            );
        } catch (MPException | MPApiException e) {
            LOGGER.error(METHOD_CREATE_PAYMENT, "Erro ao criar pagamento no Mercado Pago", e);
            throw new GatewayException(Provider.MERCADO_PAGO, "Erro ao criar pagamento: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewayPaymentResponse getPayment(String providerPaymentId) {
        LOGGER.info("getPayment", "Buscando pagamento no Mercado Pago: id={}", providerPaymentId);

        try {
            MercadoPagoConfig.setAccessToken(mpAccessToken);
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(Long.parseLong(providerPaymentId));

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("status", payment.getStatus());
            metadata.put("status_detail", payment.getStatusDetail());

            return new GatewayPaymentResponse(
                    payment.getId().toString(),
                    payment.getStatus(),
                    payment.getTransactionAmount(),
                    payment.getCurrencyId(),
                    null,
                    null,
                    null,
                    null,
                    metadata
            );
        } catch (MPException | MPApiException e) {
            LOGGER.error("getPayment", "Erro ao buscar pagamento no Mercado Pago", e);
            throw new GatewayException(Provider.MERCADO_PAGO, "Erro ao buscar pagamento: " + e.getMessage(), e);
        }
    }

    @Override
    public List<GatewayPaymentResponse> listPayments(String customerId, Integer offset, Integer limit) {
        LOGGER.info("listPayments", "Listando pagamentos no Mercado Pago: customerId={}, offset={}, limit={}", 
                customerId, offset, limit);
        throw new GatewayException(Provider.MERCADO_PAGO, "Listagem de pagamentos não implementada para Mercado Pago");
    }

    @Override
    public void cancelPayment(String providerPaymentId) {
        LOGGER.info(METHOD_CANCEL_PAYMENT, "Cancelando pagamento no Mercado Pago: id={}", providerPaymentId);

        try {
            MercadoPagoConfig.setAccessToken(mpAccessToken);
            PaymentClient client = new PaymentClient();
            client.cancel(Long.parseLong(providerPaymentId));
            LOGGER.info(METHOD_CANCEL_PAYMENT, "Pagamento cancelado: id={}", providerPaymentId);
        } catch (MPException | MPApiException e) {
            LOGGER.error(METHOD_CANCEL_PAYMENT, "Erro ao cancelar pagamento no Mercado Pago", e);
            throw new GatewayException(Provider.MERCADO_PAGO, "Erro ao cancelar pagamento: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewayPaymentResponse restorePayment(String providerPaymentId) {
        LOGGER.info("restorePayment", "Restaurando pagamento no Mercado Pago: id={}", providerPaymentId);
        throw new GatewayException(Provider.MERCADO_PAGO, "Restauração de pagamento não implementada para Mercado Pago");
    }

    private String extractTicketUrl(Payment payment) {
        try {
            if (payment.getPointOfInteraction() != null) {
                Object transactionData = payment.getPointOfInteraction().getTransactionData();
                if (transactionData != null) {
                    try {
                        return (String) transactionData.getClass().getMethod("getTicketUrl").invoke(transactionData);
                    } catch (Exception e) {
                        LOGGER.warn("extractTicketUrl", "Método getTicketUrl não encontrado");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("extractTicketUrl", "Erro ao extrair ticket URL: {}", e.getMessage());
        }
        return null;
    }

    private void setTransactionAmount(Object requestBuilder, BigDecimal amount) throws Exception {
        requestBuilder.getClass().getMethod("transactionAmount", BigDecimal.class)
                .invoke(requestBuilder, amount);
    }

    private void setDescription(Object requestBuilder, String description) throws Exception {
        requestBuilder.getClass().getMethod("description", String.class)
                .invoke(requestBuilder, description);
    }

    private void setExternalReference(Object requestBuilder, String externalId) throws Exception {
        requestBuilder.getClass().getMethod("externalReference", String.class)
                .invoke(requestBuilder, externalId);
    }

    private void setCurrencyId(Object requestBuilder, String currency) {
        try {
            requestBuilder.getClass().getMethod("currencyId", String.class)
                    .invoke(requestBuilder, currency);
        } catch (Exception e) {
            try {
                requestBuilder.getClass().getMethod("setCurrencyId", String.class)
                        .invoke(requestBuilder, currency);
            } catch (Exception e2) {
                LOGGER.warn("setCurrencyId", "Método currencyId não encontrado");
            }
        }
    }

    private Object buildPayer(Customer customer) throws Exception {
        Object payerBuilder = PaymentPayerRequest.class.getMethod("builder").invoke(null);
        String customerEmail = customer.getEmail();
        setPayerEmail(payerBuilder, customerEmail);
        return payerBuilder.getClass().getMethod("build").invoke(payerBuilder);
    }

    private void setPayerEmail(Object payerBuilder, String email) {
        try {
            payerBuilder.getClass().getMethod("email", String.class)
                    .invoke(payerBuilder, email);
        } catch (Exception e) {
            try {
                payerBuilder.getClass().getMethod("setEmail", String.class)
                        .invoke(payerBuilder, email);
            } catch (Exception e2) {
                LOGGER.warn("setPayerEmail", "Método email não encontrado no payer");
            }
        }
    }

    private void setPayer(Object requestBuilder, Object payer) throws Exception {
        try {
            requestBuilder.getClass().getMethod("payer", PaymentPayerRequest.class)
                    .invoke(requestBuilder, payer);
        } catch (Exception e) {
            requestBuilder.getClass().getMethod("payer", Object.class)
                    .invoke(requestBuilder, payer);
        }
    }
}

