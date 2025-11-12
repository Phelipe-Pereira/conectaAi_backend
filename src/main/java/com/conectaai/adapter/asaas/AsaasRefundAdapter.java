package com.conectaai.adapter.asaas;

import com.asaas.apisdk.AsaasSdk;
import com.conectaai.adapter.gateway.GatewayRefundResponse;
import com.conectaai.adapter.gateway.RefundGatewayAdapter;
import com.conectaai.domain.Payment;
import com.conectaai.enums.Provider;
import com.conectaai.exception.GatewayException;
import com.conectaai.logger.AppLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AsaasRefundAdapter implements RefundGatewayAdapter {

    private static final AppLogger LOGGER = AppLogger.getLogger(AsaasRefundAdapter.class);
    
    private static final String FIELD_VALUE = "value";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_DATE_CREATED = "dateCreated";

    private final AsaasSdk asaasSdk;

    @Override
    public GatewayRefundResponse createRefund(
            Payment payment,
            BigDecimal amount,
            String reason,
            String externalId) {
        LOGGER.info("createRefund", "Criando reembolso no Asaas: payment={}, amount={}", 
                payment.getProviderPaymentId(), amount);

        try {
            Object refundService = getRefundService(asaasSdk);
            Object request = createRefundRequest(amount, reason);
            Object response = invokeMethod(refundService, "create", payment.getProviderPaymentId(), request);

            return mapToGatewayResponse(response);
        } catch (Exception e) {
            LOGGER.error("createRefund", "Erro ao criar reembolso no Asaas", e);
            throw new GatewayException(Provider.ASAAS, "Erro ao criar reembolso: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewayRefundResponse getRefund(String providerRefundId) {
        LOGGER.info("getRefund", "Buscando reembolso no Asaas: id={}", providerRefundId);

        try {
            Object refundService = getRefundService(asaasSdk);
            Object response = invokeMethod(refundService, "getById", providerRefundId);
            return mapToGatewayResponse(response);
        } catch (Exception e) {
            LOGGER.error("getRefund", "Erro ao buscar reembolso no Asaas", e);
            throw new GatewayException(Provider.ASAAS, "Erro ao buscar reembolso: " + e.getMessage(), e);
        }
    }

    private Object createRefundRequest(BigDecimal amount, String reason) {
        try {
            Class<?> refundClass = Class.forName("com.asaas.apisdk.models.Refund");
            Object builder = refundClass.getMethod("builder").invoke(null);

            invokeBuilderMethod(builder, FIELD_VALUE, amount.doubleValue());
            if (reason != null && !reason.isBlank()) {
                invokeBuilderMethod(builder, FIELD_DESCRIPTION, reason);
            }

            Method buildMethod = builder.getClass().getMethod("build");
            return buildMethod.invoke(builder);
        } catch (Exception e) {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put(FIELD_VALUE, amount.doubleValue());
            if (reason != null && !reason.isBlank()) {
                requestData.put(FIELD_DESCRIPTION, reason);
            }
            return requestData;
        }
    }

    private GatewayRefundResponse mapToGatewayResponse(Object response) {
        if (response instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) response;
            String id = extractString(map, "id");
            String status = extractString(map, "status");
            BigDecimal amount = extractBigDecimal(map, "value");

            Map<String, Object> metadata = new HashMap<>();
            if (map.containsKey(FIELD_DATE_CREATED)) {
                metadata.put(FIELD_DATE_CREATED, map.get(FIELD_DATE_CREATED));
            }

            java.time.OffsetDateTime processedAt = null;
            if (map.containsKey(FIELD_DATE_CREATED)) {
                Object dateCreated = map.get(FIELD_DATE_CREATED);
                if (dateCreated != null) {
                    try {
                        processedAt = java.time.OffsetDateTime.parse(dateCreated.toString());
                    } catch (Exception e) {
                        LOGGER.warn("mapToGatewayResponse", "Erro ao parsear dateCreated: {}", e.getMessage());
                    }
                }
            }

            return new GatewayRefundResponse(
                    id,
                    status,
                    amount,
                    processedAt,
                    metadata
            );
        }
        throw new GatewayException(Provider.ASAAS, "Resposta inválida do gateway");
    }

    private Object getRefundService(AsaasSdk asaasSdk) {
        try {
            Method method = asaasSdk.getClass().getMethod("refundService");
            return method.invoke(asaasSdk);
        } catch (NoSuchMethodException e) {
            Method[] methods = asaasSdk.getClass().getMethods();
            for (Method m : methods) {
                if (m.getName().equals("refundService") || m.getName().equals("getRefundService")) {
                    try {
                        return m.invoke(asaasSdk);
                    } catch (Exception ex) {
                        LOGGER.error("getRefundService", "Erro ao obter RefundService", ex);
                    }
                }
            }
            throw new GatewayException(Provider.ASAAS, "Método refundService não encontrado", e);
        } catch (Exception e) {
            throw new GatewayException(Provider.ASAAS, "Erro ao obter RefundService", e);
        }
    }

    private Object invokeMethod(Object service, String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
            }
            Method method = service.getClass().getMethod(methodName, paramTypes);
            return method.invoke(service, args);
        } catch (NoSuchMethodException e) {
            Method[] methods = service.getClass().getMethods();
            for (Method m : methods) {
                if (m.getName().equals(methodName) && m.getParameterCount() == args.length) {
                    try {
                        return m.invoke(service, args);
                    } catch (Exception ex) {
                        LOGGER.error("invokeMethod", "Erro ao invocar método " + methodName, ex);
                    }
                }
            }
            throw new GatewayException(Provider.ASAAS, "Método " + methodName + " não encontrado", e);
        } catch (Exception e) {
            throw new GatewayException(Provider.ASAAS, "Erro ao invocar método " + methodName, e);
        }
    }

    private void invokeBuilderMethod(Object builder, String methodName, Object value) {
        try {
            Method method = builder.getClass().getMethod(methodName, value.getClass());
            method.invoke(builder, value);
        } catch (Exception e) {
            try {
                Method method = builder.getClass().getMethod(methodName, Object.class);
                method.invoke(builder, value);
            } catch (Exception ex) {
                LOGGER.warn("invokeBuilderMethod", "Erro ao invocar {} com valor {}", methodName, value);
            }
        }
    }

    private String extractString(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private BigDecimal extractBigDecimal(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
}

