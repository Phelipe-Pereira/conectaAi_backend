package com.conectaai.adapter.stripe;

import com.conectaai.adapter.gateway.GatewayWebhookResponse;
import com.conectaai.adapter.gateway.WebhookGatewayAdapter;
import com.conectaai.enums.Provider;
import com.conectaai.exception.GatewayException;
import com.conectaai.logger.AppLogger;
import com.stripe.exception.StripeException;
import com.stripe.model.WebhookEndpoint;
import com.stripe.param.WebhookEndpointCreateParams;
import com.stripe.param.WebhookEndpointUpdateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StripeWebhookAdapter implements WebhookGatewayAdapter {

    private static final AppLogger LOGGER = AppLogger.getLogger(StripeWebhookAdapter.class);
    private static final String METHOD_CREATE_WEBHOOK = "createWebhook";
    private static final String METHOD_GET_WEBHOOK = "getWebhook";
    private static final String METHOD_UPDATE_WEBHOOK = "updateWebhook";
    private static final String METHOD_DELETE_WEBHOOK = "deleteWebhook";
    private static final String METADATA_LIVEMODE = "livemode";
    private static final String METADATA_API_VERSION = "api_version";
    private static final String METADATA_STATUS = "status";

    @Override
    public GatewayWebhookResponse createWebhook(String url, List<String> events, String secret) {
        LOGGER.info(METHOD_CREATE_WEBHOOK, "Criando webhook no Stripe: url={}, events={}", url, events);

        try {
            WebhookEndpointCreateParams.Builder paramsBuilder = WebhookEndpointCreateParams.builder()
                    .setUrl(url);

            if (events != null && !events.isEmpty()) {
                for (String event : events) {
                    WebhookEndpointCreateParams.EnabledEvent enabledEvent = convertToEnabledEvent(event);
                    paramsBuilder.addEnabledEvent(enabledEvent);
                }
            }

            if (secret != null && !secret.isBlank()) {
                paramsBuilder.setApiVersion(WebhookEndpointCreateParams.ApiVersion.VERSION_2020_08_27);
            }

            WebhookEndpointCreateParams params = paramsBuilder.build();

            WebhookEndpoint endpoint = WebhookEndpoint.create(params);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put(METADATA_LIVEMODE, endpoint.getLivemode());
            metadata.put(METADATA_API_VERSION, endpoint.getApiVersion());
            metadata.put(METADATA_STATUS, endpoint.getStatus());

            String providerSecret = endpoint.getSecret() != null 
                    ? endpoint.getSecret() 
                    : secret;

            LOGGER.info(METHOD_CREATE_WEBHOOK, "Webhook criado no Stripe: id={}", endpoint.getId());

            return new GatewayWebhookResponse(
                    endpoint.getId(),
                    endpoint.getUrl(),
                    providerSecret,
                    metadata
            );

        } catch (StripeException e) {
            LOGGER.error(METHOD_CREATE_WEBHOOK, "Erro ao criar webhook no Stripe", e);
            throw new GatewayException(Provider.STRIPE, "Erro ao criar webhook: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(METHOD_CREATE_WEBHOOK, "Erro inesperado ao criar webhook no Stripe", e);
            throw new GatewayException(Provider.STRIPE, "Erro inesperado ao criar webhook", e);
        }
    }

    @Override
    public GatewayWebhookResponse getWebhook(String providerEndpointId) {
        LOGGER.info(METHOD_GET_WEBHOOK, "Buscando webhook no Stripe: id={}", providerEndpointId);

        try {
            WebhookEndpoint endpoint = WebhookEndpoint.retrieve(providerEndpointId);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put(METADATA_LIVEMODE, endpoint.getLivemode());
            metadata.put(METADATA_API_VERSION, endpoint.getApiVersion());
            metadata.put(METADATA_STATUS, endpoint.getStatus());
            metadata.put("enabled_events", endpoint.getEnabledEvents());

            return new GatewayWebhookResponse(
                    endpoint.getId(),
                    endpoint.getUrl(),
                    endpoint.getSecret(),
                    metadata
            );

        } catch (StripeException e) {
            LOGGER.error(METHOD_GET_WEBHOOK, "Erro ao buscar webhook no Stripe", e);
            throw new GatewayException(Provider.STRIPE, "Erro ao buscar webhook: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(METHOD_GET_WEBHOOK, "Erro inesperado ao buscar webhook no Stripe", e);
            throw new GatewayException(Provider.STRIPE, "Erro inesperado ao buscar webhook", e);
        }
    }

    @Override
    public GatewayWebhookResponse updateWebhook(String providerEndpointId, String url, List<String> events) {
        LOGGER.info(METHOD_UPDATE_WEBHOOK, "Atualizando webhook no Stripe: id={}, url={}, events={}", 
                providerEndpointId, url, events);

        try {
            WebhookEndpoint existingEndpoint = WebhookEndpoint.retrieve(providerEndpointId);

            WebhookEndpointUpdateParams.Builder paramsBuilder = WebhookEndpointUpdateParams.builder();

            if (url != null && !url.isBlank()) {
                paramsBuilder.setUrl(url);
            } else {
                paramsBuilder.setUrl(existingEndpoint.getUrl());
            }

            if (events != null && !events.isEmpty()) {
                for (String event : events) {
                    WebhookEndpointUpdateParams.EnabledEvent enabledEvent = convertToEnabledEventForUpdate(event);
                    paramsBuilder.addEnabledEvent(enabledEvent);
                }
            }

            WebhookEndpointUpdateParams params = paramsBuilder.build();

            WebhookEndpoint endpoint = existingEndpoint.update(params);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put(METADATA_LIVEMODE, endpoint.getLivemode());
            metadata.put(METADATA_API_VERSION, endpoint.getApiVersion());
            metadata.put(METADATA_STATUS, endpoint.getStatus());

            LOGGER.info(METHOD_UPDATE_WEBHOOK, "Webhook atualizado no Stripe: id={}", endpoint.getId());

            return new GatewayWebhookResponse(
                    endpoint.getId(),
                    endpoint.getUrl(),
                    endpoint.getSecret(),
                    metadata
            );

        } catch (StripeException e) {
            LOGGER.error(METHOD_UPDATE_WEBHOOK, "Erro ao atualizar webhook no Stripe", e);
            throw new GatewayException(Provider.STRIPE, "Erro ao atualizar webhook: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(METHOD_UPDATE_WEBHOOK, "Erro inesperado ao atualizar webhook no Stripe", e);
            throw new GatewayException(Provider.STRIPE, "Erro inesperado ao atualizar webhook", e);
        }
    }

    @Override
    public void deleteWebhook(String providerEndpointId) {
        LOGGER.info(METHOD_DELETE_WEBHOOK, "Deletando webhook no Stripe: id={}", providerEndpointId);

        try {
            WebhookEndpoint endpoint = WebhookEndpoint.retrieve(providerEndpointId);
            endpoint.delete();

            LOGGER.info(METHOD_DELETE_WEBHOOK, "Webhook deletado no Stripe: id={}", providerEndpointId);

        } catch (StripeException e) {
            LOGGER.error(METHOD_DELETE_WEBHOOK, "Erro ao deletar webhook no Stripe", e);
            throw new GatewayException(Provider.STRIPE, "Erro ao deletar webhook: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(METHOD_DELETE_WEBHOOK, "Erro inesperado ao deletar webhook no Stripe", e);
            throw new GatewayException(Provider.STRIPE, "Erro inesperado ao deletar webhook", e);
        }
    }

    @SuppressWarnings("unchecked")
    private WebhookEndpointCreateParams.EnabledEvent convertToEnabledEvent(String event) {
        try {
            Class<? extends Enum> enabledEventClass = WebhookEndpointCreateParams.EnabledEvent.class;
            return tryFromValue(enabledEventClass, event);
        } catch (Exception e) {
            LOGGER.error("convertToEnabledEvent", "Erro ao converter evento '{}' para EnabledEvent", event, e);
            throw new GatewayException(Provider.STRIPE, "Erro ao converter evento: " + event, e);
        }
    }

    private WebhookEndpointCreateParams.EnabledEvent tryFromValue(
            Class<? extends Enum> enabledEventClass, String event) throws Exception {
        try {
            Method fromValueMethod = enabledEventClass.getMethod("fromValue", String.class);
            return (WebhookEndpointCreateParams.EnabledEvent) fromValueMethod.invoke(null, event);
        } catch (NoSuchMethodException e) {
            return tryValueOf(enabledEventClass, event);
        }
    }

    private WebhookEndpointCreateParams.EnabledEvent tryValueOf(
            Class<? extends Enum> enabledEventClass, String event) throws Exception {
        try {
            Method valueOfMethod = enabledEventClass.getMethod("valueOf", String.class);
            String normalizedEvent = event.toUpperCase().replace(".", "_").replace("-", "_");
            return (WebhookEndpointCreateParams.EnabledEvent) valueOfMethod.invoke(null, normalizedEvent);
        } catch (NoSuchMethodException | IllegalArgumentException ex) {
            return findEnumConstant(enabledEventClass, event, ex);
        }
    }

    private WebhookEndpointCreateParams.EnabledEvent findEnumConstant(
            Class<? extends Enum> enabledEventClass, String event, Exception ex) throws Exception {
        Enum<?>[] constants = enabledEventClass.getEnumConstants();
        if (constants != null) {
            String normalizedEvent = event.toLowerCase();
            for (Enum<?> constant : constants) {
                if (constant.name().equalsIgnoreCase(normalizedEvent) 
                        || constant.toString().equalsIgnoreCase(normalizedEvent)) {
                    return (WebhookEndpointCreateParams.EnabledEvent) constant;
                }
            }
        }
        throw new GatewayException(Provider.STRIPE, "Evento não encontrado: " + event, ex);
    }

    @SuppressWarnings("unchecked")
    private WebhookEndpointUpdateParams.EnabledEvent convertToEnabledEventForUpdate(String event) {
        try {
            Class<? extends Enum> enabledEventClass = WebhookEndpointUpdateParams.EnabledEvent.class;
            return tryFromValueForUpdate(enabledEventClass, event);
        } catch (Exception e) {
            LOGGER.warn("convertToEnabledEventForUpdate",
                    "Erro ao converter para EnabledEvent de Update, tentando EnabledEvent de Create", e);
            WebhookEndpointCreateParams.EnabledEvent createEvent = convertToEnabledEvent(event);
            return (WebhookEndpointUpdateParams.EnabledEvent) (Object) createEvent;
        }
    }

    private WebhookEndpointUpdateParams.EnabledEvent tryFromValueForUpdate(
            Class<? extends Enum> enabledEventClass, String event) throws Exception {
        try {
            Method fromValueMethod = enabledEventClass.getMethod("fromValue", String.class);
            return (WebhookEndpointUpdateParams.EnabledEvent) fromValueMethod.invoke(null, event);
        } catch (NoSuchMethodException e) {
            return tryValueOfForUpdate(enabledEventClass, event);
        }
    }

    private WebhookEndpointUpdateParams.EnabledEvent tryValueOfForUpdate(
            Class<? extends Enum> enabledEventClass, String event) throws Exception {
        try {
            Method valueOfMethod = enabledEventClass.getMethod("valueOf", String.class);
            String normalizedEvent = event.toUpperCase().replace(".", "_").replace("-", "_");
            return (WebhookEndpointUpdateParams.EnabledEvent) valueOfMethod.invoke(null, normalizedEvent);
        } catch (NoSuchMethodException | IllegalArgumentException ex) {
            return findEnumConstantForUpdate(enabledEventClass, event, ex);
        }
    }

    private WebhookEndpointUpdateParams.EnabledEvent findEnumConstantForUpdate(
            Class<? extends Enum> enabledEventClass, String event, Exception ex) throws Exception {
        Enum<?>[] constants = enabledEventClass.getEnumConstants();
        if (constants != null) {
            String normalizedEvent = event.toLowerCase();
            for (Enum<?> constant : constants) {
                if (constant.name().equalsIgnoreCase(normalizedEvent) 
                        || constant.toString().equalsIgnoreCase(normalizedEvent)) {
                    return (WebhookEndpointUpdateParams.EnabledEvent) constant;
                }
            }
        }
        WebhookEndpointCreateParams.EnabledEvent createEvent = convertToEnabledEvent(event);
        return (WebhookEndpointUpdateParams.EnabledEvent) (Object) createEvent;
    }
}

