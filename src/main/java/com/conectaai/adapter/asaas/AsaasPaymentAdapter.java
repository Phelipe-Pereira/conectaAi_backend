package com.conectaai.adapter.asaas;

import com.asaas.apisdk.AsaasSdk;
import com.conectaai.adapter.gateway.GatewayPaymentResponse;
import com.conectaai.adapter.gateway.PaymentGatewayAdapter;
import com.conectaai.domain.Customer;
import com.conectaai.enums.Provider;
import com.conectaai.exception.GatewayException;
import com.conectaai.logger.AppLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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

    private final AsaasSdk asaasSdk;

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
            Object paymentService = getPaymentService(asaasSdk);
            Object request = createPaymentRequest(customer, amount, paymentMethod, 
                    description, dueDate, externalId);
            Object response = invokeMethod(paymentService, "create", request);

            return mapToGatewayResponse(response);
        } catch (Exception e) {
            LOGGER.error("createPayment", "Erro ao criar pagamento no Asaas", e);
            throw new GatewayException(Provider.ASAAS, "Erro ao criar pagamento: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewayPaymentResponse getPayment(String providerPaymentId) {
        LOGGER.info("getPayment", "Buscando pagamento no Asaas: id={}", providerPaymentId);

        try {
            Object paymentService = getPaymentService(asaasSdk);
            Object response = invokeMethod(paymentService, "getById", providerPaymentId);
            return mapToGatewayResponse(response);
        } catch (Exception e) {
            LOGGER.error("getPayment", "Erro ao buscar pagamento no Asaas", e);
            throw new GatewayException(Provider.ASAAS, "Erro ao buscar pagamento: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelPayment(String providerPaymentId) {
        LOGGER.info("cancelPayment", "Cancelando pagamento no Asaas: id={}", providerPaymentId);

        try {
            Object paymentService = getPaymentService(asaasSdk);
            invokeMethod(paymentService, "delete", providerPaymentId);
        } catch (Exception e) {
            LOGGER.error("cancelPayment", "Erro ao cancelar pagamento no Asaas", e);
            throw new GatewayException(Provider.ASAAS, "Erro ao cancelar pagamento: " + e.getMessage(), e);
        }
    }

    private Object createPaymentRequest(Customer customer, BigDecimal amount,
                                       String paymentMethod, String description, LocalDate dueDate,
                                       String externalId) {
        try {
            Class<?> paymentClass = Class.forName("com.asaas.apisdk.models.Payment");
            Object builder = paymentClass.getMethod("builder").invoke(null);

            String customerId = getCustomerId(customer);
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

            Method buildMethod = builder.getClass().getMethod("build");
            return buildMethod.invoke(builder);
        } catch (Exception e) {
            Map<String, Object> requestData = new HashMap<>();
            String customerId = getCustomerId(customer);
            requestData.put(FIELD_CUSTOMER, customerId);
            requestData.put(FIELD_VALUE, amount.doubleValue());
            requestData.put(FIELD_BILLING_TYPE, mapPaymentMethod(paymentMethod));
            requestData.put(FIELD_DUE_DATE, dueDate.format(DATE_FORMATTER));
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
            case "BOLETO" -> PAYMENT_METHOD_BOLETO;
            case "CREDIT_CARD", "CARD" -> "CREDIT_CARD";
            case "DEBIT_CARD" -> "DEBIT_CARD";
            default -> PAYMENT_METHOD_BOLETO;
        };
    }

    private GatewayPaymentResponse mapToGatewayResponse(Object response) {
        if (response instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) response;
            String id = extractString(map, "id");
            String status = extractString(map, "status");
            BigDecimal amount = extractBigDecimal(map, "value");
            String paymentUrl = extractString(map, "invoiceUrl");
            String qrCode = extractString(map, "pixQrCode");
            String barCode = extractString(map, "barcode");

            Map<String, Object> metadata = new HashMap<>();
            if (map.containsKey(FIELD_DATE_CREATED)) {
                metadata.put(FIELD_DATE_CREATED, map.get(FIELD_DATE_CREATED));
            }
            if (map.containsKey(FIELD_CLIENT_PAYMENT_DATE)) {
                metadata.put("paidAt", map.get(FIELD_CLIENT_PAYMENT_DATE));
            }

            java.time.OffsetDateTime paidAt = null;
            if (map.containsKey(FIELD_CLIENT_PAYMENT_DATE)) {
                Object paidAtObj = map.get(FIELD_CLIENT_PAYMENT_DATE);
                if (paidAtObj != null) {
                    try {
                        paidAt = java.time.OffsetDateTime.parse(paidAtObj.toString());
                    } catch (Exception e) {
                        LOGGER.warn("mapToGatewayResponse", "Erro ao parsear paidAt: {}", e.getMessage());
                    }
                }
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
        }
        throw new GatewayException(Provider.ASAAS, "Resposta inválida do gateway");
    }

    private Object getPaymentService(AsaasSdk asaasSdk) {
        try {
            Method method = asaasSdk.getClass().getMethod("paymentService");
            return method.invoke(asaasSdk);
        } catch (NoSuchMethodException e) {
            Method[] methods = asaasSdk.getClass().getMethods();
            for (Method m : methods) {
                if (m.getName().equals("paymentService") || m.getName().equals("getPaymentService")) {
                    try {
                        return m.invoke(asaasSdk);
                    } catch (Exception ex) {
                        LOGGER.error("getPaymentService", "Erro ao obter PaymentService", ex);
                    }
                }
            }
            throw new GatewayException(Provider.ASAAS, "Método paymentService não encontrado", e);
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

    private String getCustomerId(Customer customer) {
        return com.conectaai.utils.CustomerGatewayUtils.getCustomerIdForGateway(customer);
    }
}
