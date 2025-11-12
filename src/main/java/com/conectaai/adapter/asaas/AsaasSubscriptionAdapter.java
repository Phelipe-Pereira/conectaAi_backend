package com.conectaai.adapter.asaas;

import com.asaas.apisdk.AsaasSdk;
import com.conectaai.adapter.gateway.GatewaySubscriptionResponse;
import com.conectaai.adapter.gateway.SubscriptionGatewayAdapter;
import com.conectaai.domain.Customer;
import com.conectaai.enums.Provider;
import com.conectaai.exception.GatewayException;
import com.conectaai.logger.AppLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AsaasSubscriptionAdapter implements SubscriptionGatewayAdapter {

    private static final AppLogger LOGGER = AppLogger.getLogger(AsaasSubscriptionAdapter.class);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private static final String FIELD_CUSTOMER = "customer";
    private static final String FIELD_VALUE = "value";
    private static final String FIELD_BILLING_TYPE = "billingType";
    private static final String FIELD_CYCLE = "cycle";
    private static final String FIELD_NEXT_DUE_DATE = "nextDueDate";
    private static final String FIELD_END_DATE = "endDate";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_EXTERNAL_REFERENCE = "externalReference";
    private static final String FIELD_DATE_CREATED = "dateCreated";
    private static final String PAYMENT_METHOD_CREDIT_CARD = "CREDIT_CARD";
    private static final String INTERVAL_MONTHLY = "MONTHLY";

    private final AsaasSdk asaasSdk;

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
        LOGGER.info("createSubscription", "Criando assinatura no Asaas: customer={}, amount={}", 
                customer.getExternalId(), amount);

        try {
            Object subscriptionService = getSubscriptionService(asaasSdk);
            Object request = createSubscriptionRequest(customer, amount, interval, 
                    paymentMethod, description, startAt, endAt, externalId);
            Object response = invokeMethod(subscriptionService, "create", request);

            return mapToGatewayResponse(response);
        } catch (Exception e) {
            LOGGER.error("createSubscription", "Erro ao criar assinatura no Asaas", e);
            throw new GatewayException(Provider.ASAAS, "Erro ao criar assinatura: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewaySubscriptionResponse getSubscription(String providerSubscriptionId) {
        LOGGER.info("getSubscription", "Buscando assinatura no Asaas: id={}", providerSubscriptionId);

        try {
            Object subscriptionService = getSubscriptionService(asaasSdk);
            Object response = invokeMethod(subscriptionService, "getById", providerSubscriptionId);
            return mapToGatewayResponse(response);
        } catch (Exception e) {
            LOGGER.error("getSubscription", "Erro ao buscar assinatura no Asaas", e);
            throw new GatewayException(Provider.ASAAS, "Erro ao buscar assinatura: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelSubscription(String providerSubscriptionId) {
        LOGGER.info("cancelSubscription", "Cancelando assinatura no Asaas: id={}", providerSubscriptionId);

        try {
            Object subscriptionService = getSubscriptionService(asaasSdk);
            invokeMethod(subscriptionService, "delete", providerSubscriptionId);
        } catch (Exception e) {
            LOGGER.error("cancelSubscription", "Erro ao cancelar assinatura no Asaas", e);
            throw new GatewayException(Provider.ASAAS, "Erro ao cancelar assinatura: " + e.getMessage(), e);
        }
    }

    private Object createSubscriptionRequest(Customer customer, BigDecimal amount,
                                            String interval, String paymentMethod, String description,
                                            LocalDateTime startAt, LocalDateTime endAt, String externalId) {
        try {
            Class<?> subscriptionClass = Class.forName("com.asaas.apisdk.models.Subscription");
            Object builder = subscriptionClass.getMethod("builder").invoke(null);

            invokeBuilderMethod(builder, FIELD_CUSTOMER, customer.getExternalId());
            invokeBuilderMethod(builder, FIELD_VALUE, amount.doubleValue());
            invokeBuilderMethod(builder, FIELD_BILLING_TYPE, mapPaymentMethod(paymentMethod));
            invokeBuilderMethod(builder, FIELD_CYCLE, mapInterval(interval));
            invokeBuilderMethod(builder, FIELD_NEXT_DUE_DATE, startAt.format(DATETIME_FORMATTER));
            
            if (endAt != null) {
                invokeBuilderMethod(builder, FIELD_END_DATE, endAt.format(DATETIME_FORMATTER));
            }
            if (description != null && !description.isBlank()) {
                invokeBuilderMethod(builder, FIELD_DESCRIPTION, description);
            }
            if (externalId != null && !externalId.isBlank()) {
                invokeBuilderMethod(builder, FIELD_EXTERNAL_REFERENCE, externalId);
            }

            Method buildMethod = builder.getClass().getMethod("build");
            return buildMethod.invoke(builder);
        } catch (Exception e) {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put(FIELD_CUSTOMER, customer.getExternalId());
            requestData.put(FIELD_VALUE, amount.doubleValue());
            requestData.put(FIELD_BILLING_TYPE, mapPaymentMethod(paymentMethod));
            requestData.put(FIELD_CYCLE, mapInterval(interval));
            requestData.put(FIELD_NEXT_DUE_DATE, startAt.format(DATETIME_FORMATTER));
            if (endAt != null) {
                requestData.put(FIELD_END_DATE, endAt.format(DATETIME_FORMATTER));
            }
            if (description != null && !description.isBlank()) {
                requestData.put(FIELD_DESCRIPTION, description);
            }
            if (externalId != null && !externalId.isBlank()) {
                requestData.put(FIELD_EXTERNAL_REFERENCE, externalId);
            }
            return requestData;
        }
    }

    private String mapPaymentMethod(String method) {
        return switch (method.toUpperCase()) {
            case "PIX" -> "PIX";
            case "BOLETO" -> "BOLETO";
            case "CREDIT_CARD", "CARD" -> PAYMENT_METHOD_CREDIT_CARD;
            case "DEBIT_CARD" -> "DEBIT_CARD";
            default -> PAYMENT_METHOD_CREDIT_CARD;
        };
    }

    private String mapInterval(String interval) {
        return switch (interval.toUpperCase()) {
            case "DAILY" -> "DAILY";
            case "WEEKLY" -> "WEEKLY";
            case "BIWEEKLY" -> "BIWEEKLY";
            case "MONTHLY" -> INTERVAL_MONTHLY;
            case "QUARTERLY" -> "QUARTERLY";
            case "SEMIANNUALLY" -> "SEMIANNUALLY";
            case "YEARLY" -> "YEARLY";
            default -> INTERVAL_MONTHLY;
        };
    }

    private GatewaySubscriptionResponse mapToGatewayResponse(Object response) {
        if (response instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) response;
            String id = extractString(map, "id");
            String status = extractString(map, "status");
            BigDecimal amount = extractBigDecimal(map, "value");

            Map<String, Object> metadata = new HashMap<>();
            if (map.containsKey(FIELD_DATE_CREATED)) {
                metadata.put(FIELD_DATE_CREATED, map.get(FIELD_DATE_CREATED));
            }
            if (map.containsKey("nextDueDate")) {
                metadata.put("nextDueDate", map.get("nextDueDate"));
            }

            java.time.OffsetDateTime currentPeriodStart = null;
            java.time.OffsetDateTime currentPeriodEnd = null;
            if (map.containsKey("nextDueDate")) {
                Object nextDueDate = map.get("nextDueDate");
                if (nextDueDate != null) {
                    try {
                        currentPeriodStart = java.time.OffsetDateTime.parse(nextDueDate.toString());
                    } catch (Exception e) {
                        LOGGER.warn("mapToGatewayResponse", "Erro ao parsear nextDueDate: {}", e.getMessage());
                    }
                }
            }

            return new GatewaySubscriptionResponse(
                    id,
                    status,
                    amount,
                    "BRL",
                    currentPeriodStart,
                    currentPeriodEnd,
                    null,
                    metadata
            );
        }
        throw new GatewayException(Provider.ASAAS, "Resposta inválida do gateway");
    }

    private Object getSubscriptionService(AsaasSdk asaasSdk) {
        try {
            Method method = asaasSdk.getClass().getMethod("subscriptionService");
            return method.invoke(asaasSdk);
        } catch (NoSuchMethodException e) {
            Method[] methods = asaasSdk.getClass().getMethods();
            for (Method m : methods) {
                if (m.getName().equals("subscriptionService") || m.getName().equals("getSubscriptionService")) {
                    try {
                        return m.invoke(asaasSdk);
                    } catch (Exception ex) {
                        LOGGER.error("getSubscriptionService", "Erro ao obter SubscriptionService", ex);
                    }
                }
            }
            throw new GatewayException(Provider.ASAAS, "Método subscriptionService não encontrado", e);
        } catch (Exception e) {
            throw new GatewayException(Provider.ASAAS, "Erro ao obter SubscriptionService", e);
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

