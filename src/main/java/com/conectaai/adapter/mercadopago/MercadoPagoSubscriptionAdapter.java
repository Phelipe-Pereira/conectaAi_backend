package com.conectaai.adapter.mercadopago;

import com.conectaai.adapter.gateway.GatewaySubscriptionResponse;
import com.conectaai.adapter.gateway.SubscriptionGatewayAdapter;
import com.conectaai.domain.Customer;
import com.conectaai.enums.Provider;
import com.conectaai.exception.GatewayException;
import com.conectaai.logger.AppLogger;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MercadoPagoSubscriptionAdapter implements SubscriptionGatewayAdapter {

    private static final AppLogger LOGGER = AppLogger.getLogger(MercadoPagoSubscriptionAdapter.class);

    @Value("${MP_ACCESS_TOKEN:}")
    private String mpAccessToken;

    @Override
    public GatewaySubscriptionResponse createSubscription(
            Customer customer,
            BigDecimal amount,
            String currency,
            String interval,
            String paymentMethod,
            String description,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String externalId) {
        LOGGER.info("createSubscription", "Criando assinatura no Mercado Pago: customer={}, amount={}", 
                customer.getExternalId(), amount);

        try {
            MercadoPagoConfig.setAccessToken(mpAccessToken);
            PreferenceClient client = new PreferenceClient();

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(description != null && !description.isBlank() ? description : "Subscription")
                    .quantity(1)
                    .unitPrice(amount)
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(java.util.Arrays.asList(item))
                    .externalReference(externalId)
                    .payer(com.mercadopago.client.preference.PreferencePayerRequest.builder()
                            .email(customer.getEmail())
                            .build())
                    .build();

            Preference preference = client.create(request);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("preference_id", preference.getId());
            metadata.put("init_point", preference.getInitPoint());

            return new GatewaySubscriptionResponse(
                    preference.getId(),
                    "pending",
                    amount,
                    currency,
                    startAt != null ? startAt.atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime() : null,
                    endAt != null ? endAt.atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime() : null,
                    null,
                    metadata
            );
        } catch (MPException | MPApiException e) {
            LOGGER.error("createSubscription", "Erro ao criar assinatura no Mercado Pago", e);
            throw new GatewayException(Provider.MERCADO_PAGO, "Erro ao criar assinatura: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewaySubscriptionResponse getSubscription(String providerSubscriptionId) {
        LOGGER.info("getSubscription", "Buscando assinatura no Mercado Pago: id={}", providerSubscriptionId);

        try {
            MercadoPagoConfig.setAccessToken(mpAccessToken);
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.get(providerSubscriptionId);

            BigDecimal amount = BigDecimal.ZERO;
            if (preference.getItems() != null && !preference.getItems().isEmpty()) {
                amount = preference.getItems().get(0).getUnitPrice();
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("preference_id", preference.getId());

            return new GatewaySubscriptionResponse(
                    preference.getId(),
                    "active",
                    amount,
                    extractCurrencyId(preference),
                    null,
                    null,
                    null,
                    metadata
            );
        } catch (MPException | MPApiException e) {
            LOGGER.error("getSubscription", "Erro ao buscar assinatura no Mercado Pago", e);
            throw new GatewayException(Provider.MERCADO_PAGO, "Erro ao buscar assinatura: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelSubscription(String providerSubscriptionId) {
        LOGGER.warn("cancelSubscription", 
                "Mercado Pago não possui API direta para cancelar assinaturas via Preference. " +
                "ID fornecido: {}", providerSubscriptionId);
        throw new GatewayException(Provider.MERCADO_PAGO, 
                "Cancelamento de assinatura deve ser feito via painel do Mercado Pago");
    }

    private String extractCurrencyId(Preference preference) {
        try {
            return extractCurrencyIdByMethod(preference);
        } catch (Exception e) {
            LOGGER.warn("extractCurrencyId", "Erro ao extrair currencyId: {}", e.getMessage());
            return "BRL";
        }
    }

    private String extractCurrencyIdByMethod(Preference preference) {
        try {
            return (String) preference.getClass().getMethod("getCurrencyId").invoke(preference);
        } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            return extractCurrencyByMethod(preference);
        }
    }

    private String extractCurrencyByMethod(Preference preference) {
        try {
            Object currency = preference.getClass().getMethod("getCurrency").invoke(preference);
            return currency != null ? currency.toString() : "BRL";
        } catch (Exception e) {
            return "BRL";
        }
    }
}

