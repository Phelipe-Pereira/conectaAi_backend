package com.conectaai.adapter.asaas;

import com.asaas.apisdk.AsaasSdk;
import com.conectaai.adapter.gateway.GatewaySubscriptionResponse;
import com.conectaai.adapter.gateway.SubscriptionGatewayAdapter;
import com.conectaai.domain.Customer;
import com.conectaai.enums.Provider;
import com.conectaai.exception.GatewayException;
import com.conectaai.logger.AppLogger;
import com.conectaai.security.SecurityUtils;
import com.conectaai.service.gateway.AsaasSdkFactory;
import com.conectaai.service.gateway.GatewayConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AsaasSubscriptionAdapter implements SubscriptionGatewayAdapter {

    private static final AppLogger LOGGER = AppLogger.getLogger(AsaasSubscriptionAdapter.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private static final String FIELD_CUSTOMER = "customer";
    private static final String FIELD_VALUE = "value";
    private static final String FIELD_BILLING_TYPE = "billingType";
    private static final String FIELD_CYCLE = "cycle";
    private static final String FIELD_NEXT_DUE_DATE = "nextDueDate";
    private static final String FIELD_END_DATE = "endDate";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_EXTERNAL_REFERENCE = "externalReference";

    private final AsaasSdkFactory asaasSdkFactory;
    private final GatewayConfigService gatewayConfigService;

    private AsaasSdk getAsaasSdk() {
        Long userId = SecurityUtils.getCurrentUserId();
        String apiKey = gatewayConfigService.getAsaasApiKey(userId);
        if (apiKey == null) {
            throw new GatewayException(Provider.ASAAS, 
                "Chave do Asaas não configurada. Configure sua chave nas configurações do gateway.");
        }
        return asaasSdkFactory.createSdkForUser(apiKey);
    }

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
            AsaasSdk asaasSdk = getAsaasSdk();
            Object subscriptionService = getSubscriptionService(asaasSdk);
            Object request = createSubscriptionRequest(customer, amount, interval, 
                    paymentMethod, description, startAt, endAt, externalId);
            Object response = invokeMethod(subscriptionService, "createNewSubscription", request);
            return mapToGatewayResponse(response);
        } catch (Exception e) {
            LOGGER.error("createSubscription", "Erro ao criar assinatura no Asaas: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao criar assinatura: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewaySubscriptionResponse getSubscription(String providerSubscriptionId) {
        LOGGER.info("getSubscription", "Buscando assinatura no Asaas: id={}", providerSubscriptionId);

        try {
            AsaasSdk asaasSdk = getAsaasSdk();
            Object subscriptionService = getSubscriptionService(asaasSdk);
            Object response = invokeMethod(subscriptionService, "retrieveASingleSubscription", providerSubscriptionId);
            return mapToGatewayResponse(response);
        } catch (Exception e) {
            LOGGER.error("getSubscription", "Erro ao buscar assinatura no Asaas: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao buscar assinatura: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelSubscription(String providerSubscriptionId) {
        LOGGER.info("cancelSubscription", "Cancelando assinatura no Asaas: id={}", providerSubscriptionId);

        try {
            AsaasSdk asaasSdk = getAsaasSdk();
            Object subscriptionService = getSubscriptionService(asaasSdk);
            invokeMethod(subscriptionService, "removeSubscription", providerSubscriptionId);
            LOGGER.info("cancelSubscription", "Assinatura cancelada no Asaas com sucesso");
        } catch (Exception e) {
            LOGGER.error("cancelSubscription", "Erro ao cancelar assinatura no Asaas: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao cancelar assinatura: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Object> listSubscriptions(String customer, String customerGroupName, String billingType, String status, String externalReference, Integer offset, Integer limit) {
        LOGGER.info("listSubscriptions", "Listando assinaturas no Asaas: customer={}, offset={}, limit={}", 
                customer, offset, limit);

        try {
            AsaasSdk asaasSdk = getAsaasSdk();
            Object subscriptionService = getSubscriptionService(asaasSdk);
            Object listParameters = createListSubscriptionsParameters(customer, customerGroupName, billingType, status, externalReference, offset, limit);
            Object response = invokeMethod(subscriptionService, "listSubscriptions", listParameters);
            return extractSubscriptionList(response);
        } catch (Exception e) {
            LOGGER.error("listSubscriptions", "Erro ao listar assinaturas no Asaas: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao listar assinaturas: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewaySubscriptionResponse updateSubscription(String providerSubscriptionId, Customer customer, BigDecimal amount, String currency, String interval, String paymentMethod, String description, LocalDateTime startAt, LocalDateTime endAt, String externalId) {
        LOGGER.info("updateSubscription", "Atualizando assinatura no Asaas: id={}", providerSubscriptionId);

        try {
            AsaasSdk asaasSdk = getAsaasSdk();
            Object subscriptionService = getSubscriptionService(asaasSdk);
            Object request = createSubscriptionUpdateRequest(customer, amount, interval, paymentMethod, description, startAt, endAt, externalId);
            Object response = invokeMethod(subscriptionService, "updateExistingSubscription", providerSubscriptionId, request);
            LOGGER.info("updateSubscription", "Assinatura atualizada no Asaas com sucesso");
            return mapToGatewayResponse(response);
        } catch (Exception e) {
            LOGGER.error("updateSubscription", "Erro ao atualizar assinatura no Asaas: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao atualizar assinatura: " + e.getMessage(), e);
        }
    }

    private Object createSubscriptionRequest(Customer customer, BigDecimal amount,
                                            String interval, String paymentMethod, String description,
                                            LocalDateTime startAt, LocalDateTime endAt, String externalId) {
        try {
            LOGGER.info("createSubscriptionRequest", "Criando SubscriptionSaveRequestDto via reflection");
            Class<?> subscriptionSaveRequestDtoClass = Class.forName("com.asaas.apisdk.models.SubscriptionSaveRequestDto");
            LOGGER.info("createSubscriptionRequest", "Classe SubscriptionSaveRequestDto carregada: {}", subscriptionSaveRequestDtoClass.getName());
            
            Method builderMethod = subscriptionSaveRequestDtoClass.getMethod("builder");
            Object builder = builderMethod.invoke(null);
            LOGGER.info("createSubscriptionRequest", "Builder criado com sucesso");
            
            String customerId = getCustomerId(customer);
            if (customerId == null || customerId.isBlank()) {
                throw new IllegalArgumentException("Customer ID é obrigatório para criar assinatura no Asaas");
            }
            
            invokeBuilderMethod(builder, FIELD_CUSTOMER, customerId);
            invokeBuilderMethod(builder, FIELD_VALUE, amount.doubleValue());
            invokeBuilderMethod(builder, FIELD_BILLING_TYPE, mapPaymentMethodToEnum(paymentMethod));
            invokeBuilderMethod(builder, FIELD_CYCLE, mapIntervalToEnum(interval));
            invokeBuilderMethod(builder, FIELD_NEXT_DUE_DATE, startAt.format(DATE_FORMATTER));
            
            if (endAt != null) {
                invokeBuilderMethod(builder, FIELD_END_DATE, endAt.format(DATE_FORMATTER));
            }
            if (description != null && !description.isBlank()) {
                invokeBuilderMethod(builder, FIELD_DESCRIPTION, description);
            }
            if (externalId != null && !externalId.isBlank()) {
                invokeBuilderMethod(builder, FIELD_EXTERNAL_REFERENCE, externalId);
            }

            Method buildMethod = builder.getClass().getMethod("build");
            Object request = buildMethod.invoke(builder);
            LOGGER.info("createSubscriptionRequest", "Request criado com sucesso");
            return request;
        } catch (IllegalArgumentException e) {
            LOGGER.error("createSubscriptionRequest", "Erro de validação: {}", e.getMessage());
            throw new GatewayException(Provider.ASAAS, "Erro ao criar request: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("createSubscriptionRequest", "Erro ao criar request via reflection: {} - {}", 
                    e.getClass().getSimpleName(), e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao criar SubscriptionSaveRequestDto: " + e.getMessage(), e);
        }
    }

    private Object createSubscriptionUpdateRequest(Customer customer, BigDecimal amount,
                                                   String interval, String paymentMethod, String description,
                                                   LocalDateTime startAt, LocalDateTime endAt, String externalId) {
        try {
            Class<?> subscriptionUpdateRequestDtoClass = Class.forName("com.asaas.apisdk.models.SubscriptionUpdateRequestDto");
            Method builderMethod = subscriptionUpdateRequestDtoClass.getMethod("builder");
            Object builder = builderMethod.invoke(null);
            
            invokeBuilderMethod(builder, FIELD_VALUE, amount.doubleValue());
            invokeBuilderMethod(builder, FIELD_BILLING_TYPE, mapPaymentMethodToEnum(paymentMethod));
            invokeBuilderMethod(builder, FIELD_CYCLE, mapIntervalToEnum(interval));
            invokeBuilderMethod(builder, FIELD_NEXT_DUE_DATE, startAt.format(DATE_FORMATTER));
            
            if (endAt != null) {
                invokeBuilderMethod(builder, FIELD_END_DATE, endAt.format(DATE_FORMATTER));
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
            LOGGER.error("createSubscriptionUpdateRequest", "Erro ao criar request de atualização: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao criar SubscriptionUpdateRequestDto: " + e.getMessage(), e);
        }
    }

    private Object mapPaymentMethodToEnum(String method) {
        try {
            Class<?> billingTypeClass = Class.forName("com.asaas.apisdk.models.SubscriptionSaveRequestBillingType");
            String enumName = switch (method.toUpperCase()) {
                case "PIX" -> "PIX";
                case "BOLETO" -> "BOLETO";
                case "CREDIT_CARD", "CARD" -> "CREDIT_CARD";
                case "DEBIT_CARD" -> "DEBIT_CARD";
                default -> "UNDEFINED";
            };
            return Enum.valueOf((Class<Enum>) billingTypeClass, enumName);
        } catch (Exception e) {
            LOGGER.warn("mapPaymentMethodToEnum", "Erro ao mapear método de pagamento, usando string: {}", e.getMessage());
            return switch (method.toUpperCase()) {
                case "PIX" -> "PIX";
                case "BOLETO" -> "BOLETO";
                case "CREDIT_CARD", "CARD" -> "CREDIT_CARD";
                case "DEBIT_CARD" -> "DEBIT_CARD";
                default -> "UNDEFINED";
            };
        }
    }

    private Object mapIntervalToEnum(String interval) {
        try {
            Class<?> cycleClass = Class.forName("com.asaas.apisdk.models.SubscriptionSaveRequestCycle");
            String enumName = switch (interval.toUpperCase()) {
                case "DAILY" -> "DAILY";
                case "WEEKLY" -> "WEEKLY";
                case "BIWEEKLY" -> "BIWEEKLY";
                case "MONTHLY" -> "MONTHLY";
                case "QUARTERLY" -> "QUARTERLY";
                case "SEMIANNUALLY" -> "SEMIANNUALLY";
                case "YEARLY" -> "YEARLY";
                default -> "MONTHLY";
            };
            return Enum.valueOf((Class<Enum>) cycleClass, enumName);
        } catch (Exception e) {
            LOGGER.warn("mapIntervalToEnum", "Erro ao mapear intervalo, usando string: {}", e.getMessage());
            return switch (interval.toUpperCase()) {
                case "DAILY" -> "DAILY";
                case "WEEKLY" -> "WEEKLY";
                case "BIWEEKLY" -> "BIWEEKLY";
                case "MONTHLY" -> "MONTHLY";
                case "QUARTERLY" -> "QUARTERLY";
                case "SEMIANNUALLY" -> "SEMIANNUALLY";
                case "YEARLY" -> "YEARLY";
                default -> "MONTHLY";
            };
        }
    }

    private GatewaySubscriptionResponse mapToGatewayResponse(Object response) {
        try {
            String id = extractField(response, "id");
            String status = extractField(response, "status");
            BigDecimal amount = extractBigDecimalField(response, "value");
            
            java.time.OffsetDateTime currentPeriodStart = null;
            Object nextDueDateObj = extractFieldObject(response, "nextDueDate");
            if (nextDueDateObj != null) {
                try {
                    if (nextDueDateObj instanceof java.time.OffsetDateTime) {
                        currentPeriodStart = (java.time.OffsetDateTime) nextDueDateObj;
                    } else {
                        currentPeriodStart = java.time.OffsetDateTime.parse(nextDueDateObj.toString());
                    }
                } catch (Exception e) {
                    LOGGER.warn("mapToGatewayResponse", "Erro ao parsear nextDueDate: {}", e.getMessage());
                }
            }

            Map<String, Object> metadata = new HashMap<>();
            Object dateCreated = extractFieldObject(response, "dateCreated");
            if (dateCreated != null) {
                metadata.put("dateCreated", dateCreated);
            }
            if (currentPeriodStart != null) {
                metadata.put("nextDueDate", currentPeriodStart);
            }

            return new GatewaySubscriptionResponse(
                    id,
                    status,
                    amount,
                    "BRL",
                    currentPeriodStart,
                    null,
                    null,
                    metadata
            );
        } catch (Exception e) {
            LOGGER.error("mapToGatewayResponse", "Erro ao mapear resposta: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao mapear resposta do gateway: " + e.getMessage(), e);
        }
    }

    private String extractField(Object obj, String fieldName) {
        try {
            Object value = extractFieldObject(obj, fieldName);
            return unwrapValue(value);
        } catch (Exception e) {
            return null;
        }
    }
    
    private Object extractFieldObject(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            return unwrapJsonNullable(value);
        } catch (NoSuchFieldException e) {
            try {
                Method method = obj.getClass().getMethod("get" + capitalize(fieldName));
                Object value = method.invoke(obj);
                return unwrapJsonNullable(value);
            } catch (Exception ex) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    private Object unwrapJsonNullable(Object value) {
        if (value == null) {
            return null;
        }
        
        try {
            Class<?> valueClass = value.getClass();
            String className = valueClass.getName();
            
            if (className.contains("JsonNullable") || className.contains("Optional")) {
                try {
                    Method getMethod = valueClass.getMethod("get");
                    Object unwrapped = getMethod.invoke(value);
                    return unwrapJsonNullable(unwrapped);
                } catch (NoSuchMethodException e) {
                    try {
                        Method getValueMethod = valueClass.getMethod("getValue");
                        Object unwrapped = getValueMethod.invoke(value);
                        return unwrapJsonNullable(unwrapped);
                    } catch (NoSuchMethodException ex) {
                        try {
                            Method orElseMethod = valueClass.getMethod("orElse", Object.class);
                            Object unwrapped = orElseMethod.invoke(value, (Object) null);
                            return unwrapJsonNullable(unwrapped);
                        } catch (NoSuchMethodException exc) {
                            try {
                                Method isPresentMethod = valueClass.getMethod("isPresent");
                                Boolean isPresent = (Boolean) isPresentMethod.invoke(value);
                                if (Boolean.TRUE.equals(isPresent)) {
                                    Method getMethod = valueClass.getMethod("get");
                                    Object unwrapped = getMethod.invoke(value);
                                    return unwrapJsonNullable(unwrapped);
                                }
                                return null;
                            } catch (Exception ex2) {
                                return value;
                            }
                        }
                    }
                }
            }
            
            return value;
        } catch (Exception e) {
            return value;
        }
    }
    
    private String unwrapValue(Object value) {
        if (value == null) {
            return null;
        }
        Object unwrapped = unwrapJsonNullable(value);
        return unwrapped != null ? unwrapped.toString() : null;
    }
    
    private BigDecimal extractBigDecimalField(Object obj, String fieldName) {
        try {
            Object value = extractFieldObject(obj, fieldName);
            if (value == null) {
                return null;
            }
            if (value instanceof BigDecimal) {
                return (BigDecimal) value;
            }
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            }
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private Object getSubscriptionService(AsaasSdk sdk) {
        try {
            Field subscriptionField = sdk.getClass().getField("subscription");
            subscriptionField.setAccessible(true);
            return subscriptionField.get(sdk);
        } catch (NoSuchFieldException e) {
            try {
                Field subscriptionField = sdk.getClass().getDeclaredField("subscription");
                subscriptionField.setAccessible(true);
                return subscriptionField.get(sdk);
            } catch (NoSuchFieldException ex) {
                Field[] allFields = sdk.getClass().getDeclaredFields();
                StringBuilder fieldNames = new StringBuilder();
                for (Field f : allFields) {
                    fieldNames.append(f.getName()).append(", ");
                }
                LOGGER.warn("getSubscriptionService", 
                        "Campo 'subscription' não encontrado. Campos disponíveis: {}", 
                        fieldNames.toString());
                throw new GatewayException(Provider.ASAAS, 
                        "Campo 'subscription' não encontrado no AsaasSdk. Campos disponíveis: " + fieldNames.toString(), ex);
            } catch (Exception ex) {
                throw new GatewayException(Provider.ASAAS, "Erro ao acessar campo 'subscription'", ex);
            }
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
            
            LOGGER.info("invokeMethod", "Tentando encontrar método {} com {} parâmetros", methodName, args.length);
            Method method = service.getClass().getMethod(methodName, paramTypes);
            LOGGER.info("invokeMethod", "Método {} encontrado, invocando", methodName);
            
            Object result = method.invoke(service, args);
            LOGGER.info("invokeMethod", "Método {} invocado com sucesso", methodName);
            return result;
        } catch (NoSuchMethodException e) {
            LOGGER.warn("invokeMethod", "Método {} não encontrado com tipos exatos, buscando alternativas", methodName);
            Method[] methods = service.getClass().getMethods();
            StringBuilder availableMethods = new StringBuilder();
            for (Method m : methods) {
                if (m.getName().equals(methodName)) {
                    availableMethods.append(m.getName()).append("(");
                    Class<?>[] params = m.getParameterTypes();
                    for (int i = 0; i < params.length; i++) {
                        availableMethods.append(params[i].getSimpleName());
                        if (i < params.length - 1) availableMethods.append(", ");
                    }
                    availableMethods.append("), ");
                }
            }
            LOGGER.warn("invokeMethod", "Métodos disponíveis com nome {}: {}", methodName, availableMethods.toString());
            
            for (Method m : methods) {
                if (m.getName().equals(methodName) && m.getParameterCount() == args.length) {
                    try {
                        LOGGER.info("invokeMethod", "Tentando invocar método alternativo: {}", m.getName());
                        return m.invoke(service, args);
                    } catch (java.lang.reflect.InvocationTargetException ex) {
                        Throwable cause = ex.getCause();
                        String errorMsg = cause != null ? cause.getMessage() : ex.getMessage();
                        LOGGER.error("invokeMethod", "Erro ao invocar método {}: {} - Causa: {}", 
                                methodName, errorMsg, cause != null ? cause : ex);
                        throw new GatewayException(Provider.ASAAS, 
                                "Erro ao invocar método " + methodName + ": " + errorMsg, 
                                cause != null ? cause : ex);
                    } catch (Exception ex) {
                        LOGGER.error("invokeMethod", "Erro ao invocar método {}: {}", methodName, ex.getMessage(), ex);
                    }
                }
            }
            throw new GatewayException(Provider.ASAAS, 
                    "Método " + methodName + " não encontrado. Métodos disponíveis: " + availableMethods.toString(), e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            String errorMsg = cause != null ? cause.getMessage() : e.getMessage();
            LOGGER.error("invokeMethod", "Erro ao invocar método {}: {} - Causa: {}", 
                    methodName, errorMsg, cause != null ? cause : e);
            throw new GatewayException(Provider.ASAAS, 
                    "Erro ao invocar método " + methodName + ": " + errorMsg, 
                    cause != null ? cause : e);
        } catch (Exception e) {
            LOGGER.error("invokeMethod", "Erro ao invocar método {}: {}", methodName, e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao invocar método " + methodName + ": " + e.getMessage(), e);
        }
    }

    private void invokeBuilderMethod(Object builder, String methodName, Object value) {
        if (value == null) {
            return;
        }
        
        try {
            Class<?> paramType = value != null ? value.getClass() : Object.class;
            Method method = builder.getClass().getMethod(methodName, paramType);
            method.invoke(builder, value);
            LOGGER.info("invokeBuilderMethod", "Método {} invocado com sucesso usando tipo {}", methodName, paramType.getSimpleName());
        } catch (NoSuchMethodException e) {
            try {
                Method method = builder.getClass().getMethod(methodName, Object.class);
                method.invoke(builder, value);
                LOGGER.info("invokeBuilderMethod", "Método {} invocado com sucesso usando tipo Object", methodName);
            } catch (NoSuchMethodException ex) {
                Method[] methods = builder.getClass().getMethods();
                for (Method m : methods) {
                    if (m.getName().equals(methodName) && m.getParameterCount() == 1) {
                        try {
                            m.invoke(builder, value);
                            LOGGER.info("invokeBuilderMethod", "Método {} invocado com sucesso usando método alternativo", methodName);
                            return;
                        } catch (Exception ignored) {
                        }
                    }
                }
                LOGGER.warn("invokeBuilderMethod", "Erro ao invocar {} com valor {}: método não encontrado", methodName, value);
            } catch (Exception ex) {
                LOGGER.warn("invokeBuilderMethod", "Erro ao invocar {} com valor {}: {}", methodName, value, ex.getMessage());
            }
        } catch (Exception e) {
            LOGGER.warn("invokeBuilderMethod", "Erro ao invocar {} com valor {}: {}", methodName, value, e.getMessage());
        }
    }

    private Object createListSubscriptionsParameters(String customer, String customerGroupName, String billingType, String status, String externalReference, Integer offset, Integer limit) {
        try {
            LOGGER.info("createListSubscriptionsParameters", "Criando ListSubscriptionsParameters via reflection");
            Class<?> listParametersClass = Class.forName("com.asaas.apisdk.models.ListSubscriptionsParameters");
            LOGGER.info("createListSubscriptionsParameters", "Classe ListSubscriptionsParameters carregada: {}", listParametersClass.getName());
            
            Method builderMethod = listParametersClass.getMethod("builder");
            Object builder = builderMethod.invoke(null);
            LOGGER.info("createListSubscriptionsParameters", "Builder criado com sucesso");
            
            if (customer != null && !customer.isBlank()) {
                invokeBuilderMethod(builder, "customer", customer);
            }
            if (customerGroupName != null && !customerGroupName.isBlank()) {
                invokeBuilderMethod(builder, "customerGroupName", customerGroupName);
            }
            if (billingType != null && !billingType.isBlank()) {
                try {
                    Class<?> billingTypeClass = Class.forName("com.asaas.apisdk.models.SubscriptionListRequestBillingType");
                    Object enumValue = Enum.valueOf((Class<Enum>) billingTypeClass, billingType.toUpperCase());
                    invokeBuilderMethod(builder, "billingType", enumValue);
                } catch (Exception e) {
                    invokeBuilderMethod(builder, "billingType", billingType);
                }
            }
            if (status != null && !status.isBlank()) {
                try {
                    Class<?> statusClass = Class.forName("com.asaas.apisdk.models.SubscriptionListRequestSubscriptionStatus");
                    Object enumValue = Enum.valueOf((Class<Enum>) statusClass, status.toUpperCase());
                    invokeBuilderMethod(builder, "status", enumValue);
                } catch (Exception e) {
                    invokeBuilderMethod(builder, "status", status);
                }
            }
            if (externalReference != null && !externalReference.isBlank()) {
                invokeBuilderMethod(builder, "externalReference", externalReference);
            }
            if (offset != null) {
                invokeBuilderMethod(builder, "offset", offset.longValue());
            }
            if (limit != null) {
                invokeBuilderMethod(builder, "limit", limit.longValue());
            }
            
            Method buildMethod = builder.getClass().getMethod("build");
            Object parameters = buildMethod.invoke(builder);
            LOGGER.info("createListSubscriptionsParameters", "ListSubscriptionsParameters criado com sucesso");
            return parameters;
        } catch (Exception e) {
            LOGGER.error("createListSubscriptionsParameters", "Erro ao criar ListSubscriptionsParameters: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao criar parâmetros de listagem: " + e.getMessage(), e);
        }
    }

    private List<Object> extractSubscriptionList(Object response) {
        try {
            List<Object> subscriptionList = new ArrayList<>();
            
            Object data = extractFieldObject(response, "data");
            if (data == null) {
                data = extractFieldObject(response, "subscriptions");
            }
            if (data == null) {
                data = response;
            }
            
            if (data instanceof List) {
                List<?> dataList = (List<?>) data;
                LOGGER.info("extractSubscriptionList", "Extraindo {} assinaturas da lista", dataList.size());
                for (Object item : dataList) {
                    subscriptionList.add(item);
                }
            } else {
                LOGGER.warn("extractSubscriptionList", "Resposta não é uma lista, adicionando como objeto único");
                subscriptionList.add(data);
            }
            
            LOGGER.info("extractSubscriptionList", "Extração concluída: {} assinaturas", subscriptionList.size());
            return subscriptionList;
        } catch (Exception e) {
            LOGGER.error("extractSubscriptionList", "Erro ao extrair lista de assinaturas: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao extrair lista de assinaturas: " + e.getMessage(), e);
        }
    }

    private String getCustomerId(Customer customer) {
        return com.conectaai.utils.CustomerGatewayUtils.getCustomerIdForGateway(customer);
    }
}
