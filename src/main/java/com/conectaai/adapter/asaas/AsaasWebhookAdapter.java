package com.conectaai.adapter.asaas;

import com.asaas.apisdk.AsaasSdk;
import com.conectaai.adapter.gateway.GatewayWebhookResponse;
import com.conectaai.adapter.gateway.WebhookGatewayAdapter;
import com.conectaai.enums.Provider;
import com.conectaai.exception.GatewayException;
import com.conectaai.logger.AppLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AsaasWebhookAdapter implements WebhookGatewayAdapter {

    private static final AppLogger LOGGER = AppLogger.getLogger(AsaasWebhookAdapter.class);

    private final AsaasSdk asaasSdk;

    @Override
    public GatewayWebhookResponse createWebhook(String url, List<String> events, String secret) {
        LOGGER.info("createWebhook", "Criando webhook no Asaas: url={}, events={}", url, events);

        try {
            Object webhookService = getWebhookService(asaasSdk);

            Object request = createWebhookRequest(url, events, secret);
            Object webhookResponse = invokeWebhookMethod(webhookService, "create", request);

            Map<String, Object> metadata = new HashMap<>();
            String webhookId = extractId(webhookResponse);
            String webhookUrl = extractUrl(webhookResponse);
            String webhookToken = extractToken(webhookResponse);

            if (webhookResponse instanceof Map) {
                Map<?, ?> responseMap = (Map<?, ?>) webhookResponse;
                if (responseMap.containsKey("email")) {
                    metadata.put("email", responseMap.get("email"));
                }
                if (responseMap.containsKey("interrupted")) {
                    metadata.put("interrupted", responseMap.get("interrupted"));
                }
                if (responseMap.containsKey("event")) {
                    metadata.put("event", responseMap.get("event"));
                }
            }

            LOGGER.info("createWebhook", "Webhook criado no Asaas: id={}", webhookId);

            return new GatewayWebhookResponse(
                    webhookId,
                    webhookUrl != null ? webhookUrl : url,
                    secret != null ? secret : webhookToken,
                    metadata
            );

        } catch (Exception e) {
            LOGGER.error("createWebhook", "Erro ao criar webhook no Asaas", e);
            throw new GatewayException(Provider.ASAAS, "Erro ao criar webhook: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewayWebhookResponse getWebhook(String providerEndpointId) {
        LOGGER.info("getWebhook", "Buscando webhook no Asaas: id={}", providerEndpointId);

        try {
            Object webhookService = getWebhookService(asaasSdk);

            Object webhookResponse = invokeWebhookMethod(webhookService, "getById", providerEndpointId);

            Map<String, Object> metadata = new HashMap<>();
            String webhookId = extractId(webhookResponse);
            String webhookUrl = extractUrl(webhookResponse);
            String webhookToken = extractToken(webhookResponse);

            if (webhookResponse instanceof Map) {
                Map<?, ?> responseMap = (Map<?, ?>) webhookResponse;
                if (responseMap.containsKey("email")) {
                    metadata.put("email", responseMap.get("email"));
                }
                if (responseMap.containsKey("interrupted")) {
                    metadata.put("interrupted", responseMap.get("interrupted"));
                }
                if (responseMap.containsKey("event")) {
                    metadata.put("event", responseMap.get("event"));
                }
            }

            return new GatewayWebhookResponse(
                    webhookId,
                    webhookUrl,
                    webhookToken,
                    metadata
            );

        } catch (Exception e) {
            LOGGER.error("getWebhook", "Erro ao buscar webhook no Asaas", e);
            throw new GatewayException(Provider.ASAAS, "Erro ao buscar webhook: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewayWebhookResponse updateWebhook(String providerEndpointId, String url, List<String> events) {
        LOGGER.info("updateWebhook", "Atualizando webhook no Asaas: id={}, url={}, events={}", 
                    providerEndpointId, url, events);

        try {
            Object webhookService = getWebhookService(asaasSdk);

            Object existingWebhook = invokeWebhookMethod(webhookService, "getById", providerEndpointId);
            String existingUrl = extractUrl(existingWebhook);

            Object request = createWebhookRequest(
                    url != null && !url.isBlank() ? url : existingUrl,
                    events,
                    null
            );

            Object webhookResponse = invokeWebhookMethod(webhookService, "update", providerEndpointId, request);

            Map<String, Object> metadata = new HashMap<>();
            String webhookId = extractId(webhookResponse);
            String webhookUrl = extractUrl(webhookResponse);
            String webhookToken = extractToken(webhookResponse);

            if (webhookResponse instanceof Map) {
                Map<?, ?> responseMap = (Map<?, ?>) webhookResponse;
                if (responseMap.containsKey("email")) {
                    metadata.put("email", responseMap.get("email"));
                }
                if (responseMap.containsKey("interrupted")) {
                    metadata.put("interrupted", responseMap.get("interrupted"));
                }
                if (responseMap.containsKey("event")) {
                    metadata.put("event", responseMap.get("event"));
                }
            }

            LOGGER.info("updateWebhook", "Webhook atualizado no Asaas: id={}", webhookId);

            return new GatewayWebhookResponse(
                    webhookId,
                    webhookUrl,
                    webhookToken,
                    metadata
            );

        } catch (Exception e) {
            LOGGER.error("updateWebhook", "Erro ao atualizar webhook no Asaas", e);
            throw new GatewayException(Provider.ASAAS, "Erro ao atualizar webhook: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteWebhook(String providerEndpointId) {
        LOGGER.info("deleteWebhook", "Deletando webhook no Asaas: id={}", providerEndpointId);

        try {
            Object webhookService = getWebhookService(asaasSdk);

            invokeWebhookMethod(webhookService, "delete", providerEndpointId);

            LOGGER.info("deleteWebhook", "Webhook deletado no Asaas: id={}", providerEndpointId);

        } catch (Exception e) {
            LOGGER.error("deleteWebhook", "Erro ao deletar webhook no Asaas", e);
            throw new GatewayException(Provider.ASAAS, "Erro ao deletar webhook: " + e.getMessage(), e);
        }
    }

    private String extractId(Object response) {
        if (response instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) response;
            Object id = map.get("id");
            return id != null ? id.toString() : "";
        }
        return "";
    }

    private String extractUrl(Object response) {
        if (response instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) response;
            Object url = map.get("url");
            return url != null ? url.toString() : null;
        }
        return null;
    }

    private String extractToken(Object response) {
        if (response instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) response;
            Object token = map.get("authorizationToken");
            return token != null ? token.toString() : null;
        }
        return null;
    }

    private Object createWebhookRequest(String url, List<String> events, String secret) {
        try {
            Class<?> webhookClass = Class.forName("com.asaas.apisdk.models.Webhook");
            Object builder = webhookClass.getMethod("builder").invoke(null);
            
            Method urlMethod = builder.getClass().getMethod("url", String.class);
            urlMethod.invoke(builder, url);
            
            if (secret != null && !secret.isBlank()) {
                Method tokenMethod = builder.getClass().getMethod("authorizationToken", String.class);
                tokenMethod.invoke(builder, secret);
            }
            
            if (events != null && !events.isEmpty()) {
                Method eventMethod = builder.getClass().getMethod("event", String.class);
                eventMethod.invoke(builder, events.get(0));
            }
            
            Method buildMethod = builder.getClass().getMethod("build");
            return buildMethod.invoke(builder);
        } catch (Exception e) {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("url", url);
            if (secret != null && !secret.isBlank()) {
                requestData.put("authorizationToken", secret);
            }
            if (events != null && !events.isEmpty()) {
                requestData.put("event", events.get(0));
            }
            return requestData;
        }
    }

    private Object getWebhookService(AsaasSdk asaasSdk) {
        try {
            Method method = asaasSdk.getClass().getMethod("webhookService");
            return method.invoke(asaasSdk);
        } catch (NoSuchMethodException e) {
            try {
                Method[] methods = asaasSdk.getClass().getMethods();
                for (Method m : methods) {
                    if (m.getName().equals("webhookService") || m.getName().equals("getWebhookService")) {
                        return m.invoke(asaasSdk);
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("getWebhookService", "Erro ao obter WebhookService", ex);
            }
            throw new GatewayException(Provider.ASAAS, "Método webhookService não encontrado no AsaasSdk", e);
        } catch (Exception e) {
            throw new GatewayException(Provider.ASAAS, "Erro ao obter WebhookService", e);
        }
    }

    private Object invokeWebhookMethod(Object webhookService, String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
            }
            
            Method method = webhookService.getClass().getMethod(methodName, paramTypes);
            return method.invoke(webhookService, args);
        } catch (NoSuchMethodException e) {
            try {
                Method[] methods = webhookService.getClass().getMethods();
                for (Method m : methods) {
                    if (m.getName().equals(methodName) && m.getParameterCount() == args.length) {
                        return m.invoke(webhookService, args);
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("invokeWebhookMethod", "Erro ao invocar método " + methodName, ex);
            }
            throw new GatewayException(Provider.ASAAS, "Método " + methodName + " não encontrado no WebhookService", e);
        } catch (Exception e) {
            throw new GatewayException(Provider.ASAAS, "Erro ao invocar método " + methodName, e);
        }
    }
}

