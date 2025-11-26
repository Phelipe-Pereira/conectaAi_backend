package com.conectaai.adapter.asaas;

import com.asaas.apisdk.AsaasSdk;
import com.conectaai.adapter.gateway.GatewayPaymentResponse;
import com.conectaai.adapter.gateway.PaymentGatewayAdapter;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AsaasPaymentAdapter implements PaymentGatewayAdapter {

    private static final AppLogger LOGGER = AppLogger.getLogger(AsaasPaymentAdapter.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private static final String FIELD_VALUE = "value";
    private static final String FIELD_CUSTOMER = "customer";
    private static final String FIELD_BILLING_TYPE = "billingType";
    private static final String FIELD_DUE_DATE = "dueDate";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_EXTERNAL_REFERENCE = "externalReference";
    private static final String FIELD_DATE_CREATED = "dateCreated";
    private static final String FIELD_CLIENT_PAYMENT_DATE = "clientPaymentDate";
    private static final String PAYMENT_METHOD_BOLETO = "BOLETO";

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
    public GatewayPaymentResponse createPayment(
            Customer customer,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            String description,
            LocalDate dueDate,
            String externalId) {
        LOGGER.info("createPayment", "Criando pagamento no Asaas: customer={}, amount={}", 
                customer.getExternalId(), amount);

        try {
            AsaasSdk asaasSdk = getAsaasSdk();
            LOGGER.info("createPayment", "Obtendo PaymentService do AsaasSdk");
            Object paymentService = getPaymentService(asaasSdk);
            LOGGER.info("createPayment", "PaymentService obtido com sucesso");
            
            LOGGER.info("createPayment", "Criando request para o Asaas");
            Object request = createPaymentRequest(customer, amount, paymentMethod, 
                    description, dueDate, externalId);
            LOGGER.info("createPayment", "Request criado com sucesso - Tipo: {} - Classe: {}", 
                    request.getClass().getSimpleName(), request.getClass().getName());
            
            LOGGER.info("createPayment", "Chamando método createNewPayment do PaymentService com request do tipo: {}", 
                    request.getClass().getName());
            Object response = invokeMethod(paymentService, "createNewPayment", request);
            LOGGER.info("createPayment", "Resposta recebida do Asaas");

            return mapToGatewayResponse(response);
        } catch (Exception e) {
            LOGGER.error("createPayment", "Erro ao criar pagamento no Asaas: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao criar pagamento: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewayPaymentResponse getPayment(String providerPaymentId) {
        LOGGER.info("getPayment", "Buscando pagamento no Asaas: id={}", providerPaymentId);

        try {
            AsaasSdk asaasSdk = getAsaasSdk();
            Object paymentService = getPaymentService(asaasSdk);
            Object response = invokeMethod(paymentService, "retrieveASinglePayment", providerPaymentId);
            return mapToGatewayResponse(response);
        } catch (Exception e) {
            LOGGER.error("getPayment", "Erro ao buscar pagamento no Asaas", e);
            throw new GatewayException(Provider.ASAAS, "Erro ao buscar pagamento: " + e.getMessage(), e);
        }
    }

    @Override
    public List<GatewayPaymentResponse> listPayments(String customerId, Integer offset, Integer limit) {
        LOGGER.info("listPayments", "Listando pagamentos no Asaas: customerId={}, offset={}, limit={}", 
                customerId, offset, limit);

        try {
            AsaasSdk asaasSdk = getAsaasSdk();
            Object paymentService = getPaymentService(asaasSdk);
            
            Object listParameters = createListPaymentsParameters(customerId, offset, limit);
            
            LOGGER.info("listPayments", "Chamando método listPayments do PaymentService");
            Object response = invokeMethod(paymentService, "listPayments", listParameters);
            
            LOGGER.info("listPayments", "Resposta recebida do Asaas, mapeando lista de pagamentos");
            return mapToListGatewayResponse(response);
        } catch (Exception e) {
            LOGGER.error("listPayments", "Erro ao listar pagamentos no Asaas: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao listar pagamentos: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelPayment(String providerPaymentId) {
        LOGGER.info("cancelPayment", "Cancelando pagamento no Asaas: id={}", providerPaymentId);

        try {
            AsaasSdk asaasSdk = getAsaasSdk();
            Object paymentService = getPaymentService(asaasSdk);
            invokeMethod(paymentService, "deletePayment", providerPaymentId);
            LOGGER.info("cancelPayment", "Pagamento cancelado no Asaas com sucesso");
        } catch (Exception e) {
            LOGGER.error("cancelPayment", "Erro ao cancelar pagamento no Asaas: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao cancelar pagamento: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewayPaymentResponse restorePayment(String providerPaymentId) {
        LOGGER.info("restorePayment", "Restaurando pagamento no Asaas: id={}", providerPaymentId);

        try {
            AsaasSdk asaasSdk = getAsaasSdk();
            Object paymentService = getPaymentService(asaasSdk);
            Object response = invokeMethod(paymentService, "restoreRemovedPayment", providerPaymentId, new Object());
            LOGGER.info("restorePayment", "Pagamento restaurado no Asaas com sucesso");
            return mapToGatewayResponse(response);
        } catch (Exception e) {
            LOGGER.error("restorePayment", "Erro ao restaurar pagamento no Asaas: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao restaurar pagamento: " + e.getMessage(), e);
        }
    }

    private Object createPaymentRequest(Customer customer, BigDecimal amount,
                                       String paymentMethod, String description, LocalDate dueDate,
                                       String externalId) {
        try {
            LOGGER.info("createPaymentRequest", "Criando PaymentSaveRequestDto via reflection");
            Class<?> paymentSaveRequestDtoClass = Class.forName("com.asaas.apisdk.models.PaymentSaveRequestDto");
            LOGGER.info("createPaymentRequest", "Classe PaymentSaveRequestDto carregada: {}", paymentSaveRequestDtoClass.getName());
            
            LOGGER.info("createPaymentRequest", "Obtendo método builder()");
            Method builderMethod = paymentSaveRequestDtoClass.getMethod("builder");
            LOGGER.info("createPaymentRequest", "Método builder() obtido, invocando");
            
            Object builder;
            try {
                builder = builderMethod.invoke(null);
                LOGGER.info("createPaymentRequest", "Builder criado com sucesso: {}", builder.getClass().getName());
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                String errorMsg = cause != null ? cause.getMessage() : e.getMessage();
                LOGGER.error("createPaymentRequest", "Erro ao invocar builder(): {} - Causa: {}", 
                        e.getClass().getSimpleName(), errorMsg, cause != null ? cause : e);
                throw new GatewayException(Provider.ASAAS, "Erro ao criar builder: " + errorMsg, cause != null ? cause : e);
            }
            
            String customerId = getCustomerId(customer);
            if (customerId == null || customerId.isBlank()) {
                throw new IllegalArgumentException("Customer ID é obrigatório para criar pagamento no Asaas");
            }
            
            LOGGER.info("createPaymentRequest", "Iniciando população dos campos no builder");
            invokeBuilderMethod(builder, FIELD_CUSTOMER, customerId);
            invokeBuilderMethod(builder, FIELD_VALUE, amount.doubleValue());
            invokeBuilderMethod(builder, FIELD_BILLING_TYPE, mapPaymentMethod(paymentMethod));
            invokeBuilderMethod(builder, FIELD_DUE_DATE, dueDate.format(DATE_FORMATTER));
            
            if (description != null && !description.isBlank()) {
                invokeBuilderMethod(builder, FIELD_DESCRIPTION, description);
            }
            if (externalId != null && !externalId.isBlank()) {
                invokeBuilderMethod(builder, FIELD_EXTERNAL_REFERENCE, externalId);
            }
            
            LOGGER.info("createPaymentRequest", "Campos populados no builder com sucesso");
            
            LOGGER.info("createPaymentRequest", "Obtendo método build()");
            Method buildMethod = builder.getClass().getMethod("build");
            LOGGER.info("createPaymentRequest", "Método build() obtido, invocando");
            
            Object request;
            try {
                request = buildMethod.invoke(builder);
                LOGGER.info("createPaymentRequest", "Request criado com sucesso: {} - Tipo: {}", 
                        request.getClass().getName(), request.getClass().getSimpleName());
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                String errorMsg = cause != null ? cause.getMessage() : e.getMessage();
                LOGGER.error("createPaymentRequest", "Erro ao invocar build(): {} - Causa: {}", 
                        e.getClass().getSimpleName(), errorMsg, cause != null ? cause : e);
                throw new GatewayException(Provider.ASAAS, "Erro ao construir request: " + errorMsg, cause != null ? cause : e);
            }
            
            return request;
        } catch (IllegalArgumentException e) {
            LOGGER.error("createPaymentRequest", "Erro de validação: {}", e.getMessage());
            throw new GatewayException(Provider.ASAAS, "Erro ao criar request: " + e.getMessage(), e);
        } catch (GatewayException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("createPaymentRequest", "Erro ao criar request via reflection: {} - {}", 
                    e.getClass().getSimpleName(), e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao criar PaymentSaveRequestDto: " + e.getMessage(), e);
        }
    }

    private Object mapPaymentMethod(String method) {
        try {
            Class<?> billingTypeClass = Class.forName("com.asaas.apisdk.models.PaymentSaveRequestBillingType");
            LOGGER.info("mapPaymentMethod", "Mapeando método de pagamento: {} para enum {}", method, billingTypeClass.getName());
            
            String enumName = switch (method.toUpperCase()) {
                case "PIX" -> "PIX";
                case "BOLETO" -> "BOLETO";
                case "CREDIT_CARD", "CARD" -> "CREDIT_CARD";
                case "DEBIT_CARD" -> "DEBIT_CARD";
                default -> "BOLETO";
            };
            
            Object enumValue = Enum.valueOf((Class<Enum>) billingTypeClass, enumName);
            LOGGER.info("mapPaymentMethod", "Enum {} mapeado com sucesso: {}", enumName, enumValue);
            return enumValue;
        } catch (ClassNotFoundException e) {
            LOGGER.warn("mapPaymentMethod", "Classe PaymentSaveRequestBillingType não encontrada, tentando usar string: {}", e.getMessage());
            return switch (method.toUpperCase()) {
                case "PIX" -> "PIX";
                case "BOLETO" -> "BOLETO";
                case "CREDIT_CARD", "CARD" -> "CREDIT_CARD";
                case "DEBIT_CARD" -> "DEBIT_CARD";
                default -> "BOLETO";
            };
        } catch (IllegalArgumentException e) {
            LOGGER.warn("mapPaymentMethod", "Valor de enum inválido, usando BOLETO como padrão: {}", e.getMessage());
            try {
                Class<?> billingTypeClass = Class.forName("com.asaas.apisdk.models.PaymentSaveRequestBillingType");
                return Enum.valueOf((Class<Enum>) billingTypeClass, "BOLETO");
            } catch (Exception ex) {
                return "BOLETO";
            }
        } catch (Exception e) {
            LOGGER.warn("mapPaymentMethod", "Erro ao mapear método de pagamento, usando string: {}", e.getMessage());
            return switch (method.toUpperCase()) {
                case "PIX" -> "PIX";
                case "BOLETO" -> "BOLETO";
                case "CREDIT_CARD", "CARD" -> "CREDIT_CARD";
                case "DEBIT_CARD" -> "DEBIT_CARD";
                default -> "BOLETO";
            };
        }
    }

    private GatewayPaymentResponse mapToGatewayResponse(Object response) {
        try {
            String id = extractField(response, "id");
            String status = extractField(response, "status");
            BigDecimal amount = extractBigDecimalField(response, "value");
            String paymentUrl = extractField(response, "invoiceUrl");
            String qrCode = extractField(response, "pixQrCode");
            String barCode = extractField(response, "barcode");
            
            java.time.OffsetDateTime paidAt = null;
            Object paidAtObj = extractFieldObject(response, "clientPaymentDate");
            if (paidAtObj != null) {
                try {
                    if (paidAtObj instanceof java.time.OffsetDateTime) {
                        paidAt = (java.time.OffsetDateTime) paidAtObj;
                    } else {
                        paidAt = java.time.OffsetDateTime.parse(paidAtObj.toString());
                    }
                } catch (Exception e) {
                    LOGGER.warn("mapToGatewayResponse", "Erro ao parsear paidAt: {}", e.getMessage());
                }
            }

            Map<String, Object> metadata = new HashMap<>();
            Object dateCreated = extractFieldObject(response, "dateCreated");
            if (dateCreated != null) {
                metadata.put(FIELD_DATE_CREATED, dateCreated);
            }
            if (paidAt != null) {
                metadata.put("paidAt", paidAt);
            }

            return new GatewayPaymentResponse(
                    id,
                    status,
                    amount,
                    "BRL",
                    paymentUrl,
                    qrCode,
                    barCode,
                    paidAt,
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
                LOGGER.info("unwrapJsonNullable", "Desempacotando JsonNullable/Optional: {}", className);
                
                try {
                    Method getMethod = valueClass.getMethod("get");
                    Object unwrapped = getMethod.invoke(value);
                    LOGGER.info("unwrapJsonNullable", "Valor desempacotado via get(): {}", unwrapped);
                    return unwrapJsonNullable(unwrapped);
                } catch (NoSuchMethodException e) {
                    try {
                        Method getValueMethod = valueClass.getMethod("getValue");
                        Object unwrapped = getValueMethod.invoke(value);
                        LOGGER.info("unwrapJsonNullable", "Valor desempacotado via getValue(): {}", unwrapped);
                        return unwrapJsonNullable(unwrapped);
                    } catch (NoSuchMethodException ex) {
                        try {
                            Method orElseMethod = valueClass.getMethod("orElse", Object.class);
                            Object unwrapped = orElseMethod.invoke(value, (Object) null);
                            LOGGER.info("unwrapJsonNullable", "Valor desempacotado via orElse(null): {}", unwrapped);
                            return unwrapJsonNullable(unwrapped);
                        } catch (NoSuchMethodException exc) {
                            try {
                                Method isPresentMethod = valueClass.getMethod("isPresent");
                                Boolean isPresent = (Boolean) isPresentMethod.invoke(value);
                                if (Boolean.TRUE.equals(isPresent)) {
                                    Method getMethod = valueClass.getMethod("get");
                                    Object unwrapped = getMethod.invoke(value);
                                    LOGGER.info("unwrapJsonNullable", "Valor desempacotado via isPresent()/get(): {}", unwrapped);
                                    return unwrapJsonNullable(unwrapped);
                                }
                                LOGGER.info("unwrapJsonNullable", "JsonNullable/Optional está vazio (isPresent=false)");
                                return null;
                            } catch (NoSuchMethodException ex2) {
                                try {
                                    Field[] fields = valueClass.getDeclaredFields();
                                    for (Field field : fields) {
                                        String fieldName = field.getName();
                                        if (fieldName.equals("value") || fieldName.equals("present") || 
                                            fieldName.equals("isPresent") || fieldName.equals("isDefined")) {
                                            field.setAccessible(true);
                                            Object fieldValue = field.get(value);
                                            if (fieldValue != null && !fieldValue.equals(value)) {
                                                LOGGER.info("unwrapJsonNullable", "Valor desempacotado via campo {}: {}", fieldName, fieldValue);
                                                return unwrapJsonNullable(fieldValue);
                                            }
                                        }
                                    }
                                    LOGGER.warn("unwrapJsonNullable", "Não foi possível desempacotar JsonNullable, tentando toString()");
                                    String strValue = value.toString();
                                    if (strValue.startsWith("JsonNullable[") && strValue.endsWith("]")) {
                                        String extracted = strValue.substring("JsonNullable[".length(), strValue.length() - 1);
                                        LOGGER.info("unwrapJsonNullable", "Valor extraído de toString(): {}", extracted);
                                        return extracted;
                                    }
                                } catch (Exception ignored) {
                                    LOGGER.warn("unwrapJsonNullable", "Erro ao tentar extrair via campos: {}", ignored.getMessage());
                                }
                            }
                        }
                    }
                }
            }
            
            return value;
        } catch (Exception e) {
            LOGGER.warn("unwrapJsonNullable", "Erro ao desempacotar valor: {}", e.getMessage());
            try {
                String strValue = value.toString();
                if (strValue.startsWith("JsonNullable[") && strValue.endsWith("]")) {
                    String extracted = strValue.substring("JsonNullable[".length(), strValue.length() - 1);
                    LOGGER.info("unwrapJsonNullable", "Valor extraído de toString() (fallback): {}", extracted);
                    return extracted;
                }
            } catch (Exception ex) {
            }
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

    private Object getPaymentService(AsaasSdk sdk) {
        try {
            Field paymentField = sdk.getClass().getField("payment");
            paymentField.setAccessible(true);
            return paymentField.get(sdk);
        } catch (NoSuchFieldException e) {
            try {
                Field paymentField = sdk.getClass().getDeclaredField("payment");
                paymentField.setAccessible(true);
                return paymentField.get(sdk);
            } catch (NoSuchFieldException ex) {
                Field[] allFields = sdk.getClass().getDeclaredFields();
                StringBuilder fieldNames = new StringBuilder();
                for (Field f : allFields) {
                    fieldNames.append(f.getName()).append(", ");
                }
                LOGGER.warn("getPaymentService", 
                        "Campo 'payment' não encontrado. Campos disponíveis: {}", 
                        fieldNames.toString());
                throw new GatewayException(Provider.ASAAS, 
                        "Campo 'payment' não encontrado no AsaasSdk. Campos disponíveis: " + fieldNames.toString(), ex);
            } catch (Exception ex) {
                throw new GatewayException(Provider.ASAAS, "Erro ao acessar campo 'payment'", ex);
            }
        } catch (Exception e) {
            throw new GatewayException(Provider.ASAAS, "Erro ao obter PaymentService", e);
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

    private String getCustomerId(Customer customer) {
        return com.conectaai.utils.CustomerGatewayUtils.getCustomerIdForGateway(customer);
    }

    private Object createListPaymentsParameters(String customerId, Integer offset, Integer limit) {
        try {
            LOGGER.info("createListPaymentsParameters", "Criando ListPaymentsParameters via reflection");
            Class<?> listParametersClass = Class.forName("com.asaas.apisdk.models.ListPaymentsParameters");
            LOGGER.info("createListPaymentsParameters", "Classe ListPaymentsParameters carregada: {}", listParametersClass.getName());
            
            Method builderMethod = listParametersClass.getMethod("builder");
            Object builder = builderMethod.invoke(null);
            LOGGER.info("createListPaymentsParameters", "Builder criado com sucesso");
            
            if (customerId != null && !customerId.isBlank()) {
                invokeBuilderMethod(builder, "customer", customerId);
            }
            if (offset != null) {
                invokeBuilderMethod(builder, "offset", offset.longValue());
            }
            if (limit != null) {
                invokeBuilderMethod(builder, "limit", limit.longValue());
            }
            
            Method buildMethod = builder.getClass().getMethod("build");
            Object parameters = buildMethod.invoke(builder);
            LOGGER.info("createListPaymentsParameters", "ListPaymentsParameters criado com sucesso");
            return parameters;
        } catch (Exception e) {
            LOGGER.error("createListPaymentsParameters", "Erro ao criar ListPaymentsParameters: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao criar parâmetros de listagem: " + e.getMessage(), e);
        }
    }

    private List<GatewayPaymentResponse> mapToListGatewayResponse(Object response) {
        try {
            List<GatewayPaymentResponse> paymentList = new ArrayList<>();
            
            Object data = extractFieldObject(response, "data");
            if (data == null) {
                data = extractFieldObject(response, "payments");
            }
            if (data == null) {
                data = response;
            }
            
            if (data instanceof List) {
                List<?> dataList = (List<?>) data;
                LOGGER.info("mapToListGatewayResponse", "Mapeando {} pagamentos", dataList.size());
                for (Object item : dataList) {
                    try {
                        GatewayPaymentResponse paymentResponse = mapToGatewayResponse(item);
                        paymentList.add(paymentResponse);
                    } catch (Exception e) {
                        LOGGER.warn("mapToListGatewayResponse", "Erro ao mapear item da lista: {}", e.getMessage());
                    }
                }
            } else {
                LOGGER.warn("mapToListGatewayResponse", "Resposta não é uma lista, tentando mapear como objeto único");
                GatewayPaymentResponse paymentResponse = mapToGatewayResponse(data);
                paymentList.add(paymentResponse);
            }
            
            LOGGER.info("mapToListGatewayResponse", "Mapeamento concluído: {} pagamentos", paymentList.size());
            return paymentList;
        } catch (Exception e) {
            LOGGER.error("mapToListGatewayResponse", "Erro ao mapear lista de pagamentos: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao mapear lista de pagamentos: " + e.getMessage(), e);
        }
    }
}
