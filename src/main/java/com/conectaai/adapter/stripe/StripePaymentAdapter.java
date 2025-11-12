package com.conectaai.adapter.stripe;

import com.conectaai.adapter.gateway.GatewayPaymentResponse;
import com.conectaai.adapter.gateway.PaymentGatewayAdapter;
import com.conectaai.domain.Customer;
import com.conectaai.enums.Provider;
import com.conectaai.exception.GatewayException;
import com.conectaai.logger.AppLogger;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StripePaymentAdapter implements PaymentGatewayAdapter {

    private static final AppLogger LOGGER = AppLogger.getLogger(StripePaymentAdapter.class);
    private static final String METHOD_CANCEL_PAYMENT = "cancelPayment";

    @Override
    public GatewayPaymentResponse createPayment(
            Customer customer,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            String description,
            LocalDate dueDate,
            String externalId) {
        LOGGER.info("createPayment", "Criando pagamento no Stripe: customer={}, amount={}", 
                customer.getExternalId(), amount);

        try {
            long amountInCents = amount.multiply(new BigDecimal("100")).longValue();
            
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency.toLowerCase())
                    .addPaymentMethodType("card");

            if (description != null && !description.isBlank()) {
                paramsBuilder.setDescription(description);
            }

            Map<String, String> metadata = new HashMap<>();
            metadata.put("external_id", externalId);
            metadata.put("customer_external_id", customer.getExternalId());
            paramsBuilder.putAllMetadata(metadata);

            PaymentIntentCreateParams params = paramsBuilder.build();
            PaymentIntent paymentIntent = PaymentIntent.create(params);

            Map<String, Object> responseMetadata = new HashMap<>();
            responseMetadata.put("client_secret", paymentIntent.getClientSecret());
            responseMetadata.put("status", paymentIntent.getStatus());

            return new GatewayPaymentResponse(
                    paymentIntent.getId(),
                    paymentIntent.getStatus(),
                    amount,
                    currency,
                    null,
                    null,
                    null,
                    null,
                    responseMetadata
            );
        } catch (StripeException e) {
            LOGGER.error("createPayment", "Erro ao criar pagamento no Stripe", e);
            throw new GatewayException(Provider.STRIPE, "Erro ao criar pagamento: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewayPaymentResponse getPayment(String providerPaymentId) {
        LOGGER.info("getPayment", "Buscando pagamento no Stripe: id={}", providerPaymentId);

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(providerPaymentId);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("status", paymentIntent.getStatus());
            metadata.put("amount_received", paymentIntent.getAmountReceived());

            return new GatewayPaymentResponse(
                    paymentIntent.getId(),
                    paymentIntent.getStatus(),
                    BigDecimal.valueOf(paymentIntent.getAmount()).divide(new BigDecimal("100")),
                    paymentIntent.getCurrency().toUpperCase(),
                    null,
                    null,
                    null,
                    null,
                    metadata
            );
        } catch (StripeException e) {
            LOGGER.error("getPayment", "Erro ao buscar pagamento no Stripe", e);
            throw new GatewayException(Provider.STRIPE, "Erro ao buscar pagamento: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelPayment(String providerPaymentId) {
        LOGGER.info(METHOD_CANCEL_PAYMENT, "Cancelando pagamento no Stripe: id={}", providerPaymentId);

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(providerPaymentId);
            PaymentIntent canceled = paymentIntent.cancel();
            LOGGER.info(METHOD_CANCEL_PAYMENT, "Pagamento cancelado: id={}, status={}", 
                    canceled.getId(), canceled.getStatus());
        } catch (StripeException e) {
            LOGGER.error(METHOD_CANCEL_PAYMENT, "Erro ao cancelar pagamento no Stripe", e);
            throw new GatewayException(Provider.STRIPE, "Erro ao cancelar pagamento: " + e.getMessage(), e);
        }
    }
}

