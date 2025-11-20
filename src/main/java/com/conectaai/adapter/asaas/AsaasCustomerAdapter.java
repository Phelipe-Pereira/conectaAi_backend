package com.conectaai.adapter.asaas;

import com.asaas.apisdk.AsaasSdk;
import com.conectaai.adapter.gateway.CustomerGatewayAdapter;
import com.conectaai.domain.Customer;
import com.conectaai.enums.Provider;
import com.conectaai.exception.GatewayException;
import com.conectaai.logger.AppLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AsaasCustomerAdapter implements CustomerGatewayAdapter {

    private static final AppLogger LOGGER = AppLogger.getLogger(AsaasCustomerAdapter.class);
    
    private static final String FIELD_NAME = "name";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_PHONE = "phone";
    private static final String FIELD_MOBILE_PHONE = "mobilePhone";
    private static final String FIELD_CPF_CNPJ = "cpfCnpj";
    private static final String FIELD_POSTAL_CODE = "postalCode";
    private static final String FIELD_ADDRESS = "address";
    private static final String FIELD_ADDRESS_NUMBER = "addressNumber";
    private static final String FIELD_COMPLEMENT = "complement";
    private static final String FIELD_PROVINCE = "province";
    private static final String FIELD_EXTERNAL_REFERENCE = "externalReference";

    private final AsaasSdk asaasSdk;

    @Override
    public String createCustomer(Customer customer) {
        LOGGER.info("createCustomer", "Criando customer no Asaas: email={}, externalId={}", 
                customer.getEmail(), customer.getExternalId());

        try {
            LOGGER.info("createCustomer", "Obtendo CustomerService do AsaasSdk");
            Object customerService = getCustomerService(asaasSdk);
            LOGGER.info("createCustomer", "CustomerService obtido com sucesso");
            
            LOGGER.info("createCustomer", "Criando request para o Asaas");
            Object request = createCustomerRequest(customer);
            LOGGER.info("createCustomer", "Request criado com sucesso - Tipo: {} - Classe: {}", 
                    request.getClass().getSimpleName(), request.getClass().getName());
            
            LOGGER.info("createCustomer", "Chamando método createNewCustomer do CustomerService com request do tipo: {}", 
                    request.getClass().getName());
            Object response = invokeMethod(customerService, "createNewCustomer", request);
            LOGGER.info("createCustomer", "Resposta recebida do Asaas");
            
            String providerCustomerId = extractCustomerId(response);
            LOGGER.info("createCustomer", "Customer criado no Asaas com sucesso: providerCustomerId={}", 
                    providerCustomerId);
            
            return providerCustomerId;
        } catch (Exception e) {
            LOGGER.error("createCustomer", "Erro ao criar customer no Asaas: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao criar customer: " + e.getMessage(), e);
        }
    }

    @Override
    public Object getCustomer(String providerCustomerId) {
        LOGGER.info("getCustomer", "Buscando customer no Asaas: id={}", providerCustomerId);

        try {
            Object customerService = getCustomerService(asaasSdk);
            return invokeMethod(customerService, "retrieveASingleCustomer", providerCustomerId);
        } catch (Exception e) {
            LOGGER.error("getCustomer", "Erro ao buscar customer no Asaas", e);
            throw new GatewayException(Provider.ASAAS, "Erro ao buscar customer: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateCustomer(Customer customer) {
        if (customer.getProviderCustomerId() == null || customer.getProviderCustomerId().isBlank()) {
            throw new IllegalArgumentException("Provider customer ID é obrigatório para atualização");
        }

        LOGGER.info("updateCustomer", "Atualizando customer no Asaas: providerCustomerId={}", 
                customer.getProviderCustomerId());

        try {
            Object customerService = getCustomerService(asaasSdk);
            Object request = createCustomerUpdateRequest(customer);
            Object response = invokeMethod(customerService, "updateExistingCustomer", customer.getProviderCustomerId(), request);
            LOGGER.info("updateCustomer", "Customer atualizado no Asaas com sucesso");
        } catch (Exception e) {
            LOGGER.error("updateCustomer", "Erro ao atualizar customer no Asaas", e);
            throw new GatewayException(Provider.ASAAS, "Erro ao atualizar customer: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteCustomer(String providerCustomerId) {
        LOGGER.info("deleteCustomer", "Removendo customer do Asaas: id={}", providerCustomerId);

        try {
            Object customerService = getCustomerService(asaasSdk);
            invokeMethod(customerService, "removeCustomer", providerCustomerId);
            LOGGER.info("deleteCustomer", "Customer removido do Asaas com sucesso");
        } catch (Exception e) {
            LOGGER.error("deleteCustomer", "Erro ao remover customer do Asaas", e);
            throw new GatewayException(Provider.ASAAS, "Erro ao remover customer: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Object> listCustomers(String name, String email, String cpfCnpj, String groupName, String externalReference, Integer offset, Integer limit) {
        LOGGER.info("listCustomers", "Listando customers no Asaas: name={}, email={}, offset={}, limit={}", 
                name, email, offset, limit);

        try {
            Object customerService = getCustomerService(asaasSdk);
            Object listParameters = createListCustomersParameters(name, email, cpfCnpj, groupName, externalReference, offset, limit);
            Object response = invokeMethod(customerService, "listCustomers", listParameters);
            return extractCustomerList(response);
        } catch (Exception e) {
            LOGGER.error("listCustomers", "Erro ao listar customers no Asaas: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao listar customers: " + e.getMessage(), e);
        }
    }

    @Override
    public Object restoreCustomer(String providerCustomerId) {
        LOGGER.info("restoreCustomer", "Restaurando customer no Asaas: id={}", providerCustomerId);

        try {
            Object customerService = getCustomerService(asaasSdk);
            Object response = invokeMethod(customerService, "restoreRemovedCustomer", providerCustomerId, new Object());
            LOGGER.info("restoreCustomer", "Customer restaurado no Asaas com sucesso");
            return response;
        } catch (Exception e) {
            LOGGER.error("restoreCustomer", "Erro ao restaurar customer no Asaas: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao restaurar customer: " + e.getMessage(), e);
        }
    }

    private Object createCustomerRequest(Customer customer) {
        try {
            LOGGER.info("createCustomerRequest", "Criando CustomerSaveRequestDto via reflection");
            Class<?> customerSaveRequestDtoClass = Class.forName("com.asaas.apisdk.models.CustomerSaveRequestDto");
            LOGGER.info("createCustomerRequest", "Classe CustomerSaveRequestDto carregada: {}", customerSaveRequestDtoClass.getName());
            
            LOGGER.info("createCustomerRequest", "Obtendo método builder()");
            Method builderMethod = customerSaveRequestDtoClass.getMethod("builder");
            LOGGER.info("createCustomerRequest", "Método builder() obtido, invocando");
            
            Object builder;
            try {
                builder = builderMethod.invoke(null);
                LOGGER.info("createCustomerRequest", "Builder criado com sucesso: {}", builder.getClass().getName());
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                String errorMsg = cause != null ? cause.getMessage() : e.getMessage();
                String causeType = cause != null ? cause.getClass().getName() : "null";
                LOGGER.error("createCustomerRequest", "Erro ao invocar builder(): {} - Causa: {} - Tipo da causa: {} - StackTrace completo:", 
                        e.getClass().getSimpleName(), errorMsg, causeType, cause != null ? cause : e);
                if (cause != null) {
                    LOGGER.error("createCustomerRequest", "Stack trace da causa:", cause);
                }
                throw new GatewayException(Provider.ASAAS, "Erro ao criar builder: " + errorMsg, cause != null ? cause : e);
            } catch (IllegalArgumentException e) {
                LOGGER.error("createCustomerRequest", "IllegalArgumentException ao invocar builder(): {} - StackTrace:", e.getMessage(), e);
                throw new GatewayException(Provider.ASAAS, "Erro ao criar builder: " + e.getMessage(), e);
            } catch (Exception e) {
                LOGGER.error("createCustomerRequest", "Exceção inesperada ao invocar builder(): {} - {} - StackTrace:", 
                        e.getClass().getSimpleName(), e.getMessage(), e);
                throw new GatewayException(Provider.ASAAS, "Erro ao criar builder: " + e.getMessage(), e);
            }
            
            String fullName = customer.getFullName();
            if (fullName == null || fullName.isBlank()) {
                throw new IllegalArgumentException("Nome completo é obrigatório para criar customer no Asaas");
            }
            
            String document = customer.getCpf() != null && !customer.getCpf().isBlank() 
                    ? customer.getCpf() 
                    : customer.getCnpj();
            if (document == null || document.isBlank()) {
                throw new IllegalArgumentException("CPF ou CNPJ é obrigatório para criar customer no Asaas");
            }
            
            LOGGER.info("createCustomerRequest", "Iniciando população dos campos no builder");
            try {
                populateCustomerRequest(builder, customer, true);
                LOGGER.info("createCustomerRequest", "Campos populados no builder com sucesso");
            } catch (GatewayException e) {
                LOGGER.error("createCustomerRequest", "Erro ao popular campos: {}", e.getMessage(), e);
                throw e;
            }
            
            LOGGER.info("createCustomerRequest", "Obtendo método build()");
            Method buildMethod = builder.getClass().getMethod("build");
            LOGGER.info("createCustomerRequest", "Método build() obtido, invocando");
            
            Object request;
            try {
                request = buildMethod.invoke(builder);
                LOGGER.info("createCustomerRequest", "Request criado com sucesso: {} - Tipo: {}", request.getClass().getName(), request.getClass().getSimpleName());
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                String errorMsg = cause != null ? cause.getMessage() : e.getMessage();
                LOGGER.error("createCustomerRequest", "Erro ao invocar build(): {} - Causa: {}", 
                        e.getClass().getSimpleName(), errorMsg, cause != null ? cause : e);
                throw new GatewayException(Provider.ASAAS, "Erro ao construir request: " + errorMsg, cause != null ? cause : e);
            }
            
            return request;
        } catch (IllegalArgumentException e) {
            LOGGER.error("createCustomerRequest", "Erro de validação: {}", e.getMessage());
            throw new GatewayException(Provider.ASAAS, "Erro ao criar request: " + e.getMessage(), e);
        } catch (GatewayException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("createCustomerRequest", "Erro ao criar request via reflection: {} - {}", 
                    e.getClass().getSimpleName(), e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao criar CustomerSaveRequestDto: " + e.getMessage(), e);
        }
    }

    private Object createCustomerUpdateRequest(Customer customer) {
        try {
            Class<?> customerUpdateRequestDtoClass = Class.forName("com.asaas.apisdk.models.CustomerUpdateRequestDto");
            Object builder = customerUpdateRequestDtoClass.getMethod("builder").invoke(null);
            populateCustomerRequest(builder, customer, true);
            Method buildMethod = builder.getClass().getMethod("build");
            return buildMethod.invoke(builder);
        } catch (Exception e) {
            LOGGER.warn("createCustomerUpdateRequest", "Erro ao criar request via reflection, usando fallback", e);
            Map<String, Object> requestData = new HashMap<>();
            populateCustomerRequest(requestData, customer, false);
            return requestData;
        }
    }

    private void populateCustomerRequest(Object target, Customer customer, boolean useBuilder) {
        try {
            String fullName = customer.getFullName();
            if (fullName != null && !fullName.isBlank()) {
                LOGGER.info("populateCustomerRequest", "Definindo campo name: {}", fullName);
                try {
                    setField(target, FIELD_NAME, fullName, useBuilder);
                    LOGGER.info("populateCustomerRequest", "Campo name definido com sucesso");
                } catch (Exception e) {
                    LOGGER.error("populateCustomerRequest", "Erro ao definir campo name: {}", e.getMessage(), e);
                    throw new GatewayException(Provider.ASAAS, "Erro ao definir campo name: " + e.getMessage(), e);
                }
            }

            String document = customer.getCpf() != null && !customer.getCpf().isBlank() 
                    ? customer.getCpf() 
                    : customer.getCnpj();
            if (document != null && !document.isBlank()) {
                LOGGER.info("populateCustomerRequest", "Definindo campo cpfCnpj: {}", document);
                try {
                    setField(target, FIELD_CPF_CNPJ, document, useBuilder);
                    LOGGER.info("populateCustomerRequest", "Campo cpfCnpj definido com sucesso");
                } catch (Exception e) {
                    LOGGER.error("populateCustomerRequest", "Erro ao definir campo cpfCnpj: {}", e.getMessage(), e);
                    throw new GatewayException(Provider.ASAAS, "Erro ao definir campo cpfCnpj: " + e.getMessage(), e);
                }
            }

            if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
                LOGGER.info("populateCustomerRequest", "Definindo campo email: {}", customer.getEmail());
                try {
                    setField(target, FIELD_EMAIL, customer.getEmail(), useBuilder);
                    LOGGER.info("populateCustomerRequest", "Campo email definido com sucesso");
                } catch (Exception e) {
                    LOGGER.error("populateCustomerRequest", "Erro ao definir campo email: {}", e.getMessage(), e);
                    throw new GatewayException(Provider.ASAAS, "Erro ao definir campo email: " + e.getMessage(), e);
                }
            }

            if (customer.getPhone() != null && !customer.getPhone().isBlank()) {
                LOGGER.info("populateCustomerRequest", "Definindo campos phone e mobilePhone: {}", customer.getPhone());
                try {
                    setField(target, FIELD_PHONE, customer.getPhone(), useBuilder);
                    LOGGER.info("populateCustomerRequest", "Campo phone definido com sucesso");
                    setField(target, FIELD_MOBILE_PHONE, customer.getPhone(), useBuilder);
                    LOGGER.info("populateCustomerRequest", "Campo mobilePhone definido com sucesso");
                } catch (Exception e) {
                    LOGGER.error("populateCustomerRequest", "Erro ao definir campos phone/mobilePhone: {}", e.getMessage(), e);
                    throw new GatewayException(Provider.ASAAS, "Erro ao definir campos phone/mobilePhone: " + e.getMessage(), e);
                }
            }

            if (customer.getAddress() != null && !customer.getAddress().isBlank()) {
                LOGGER.info("populateCustomerRequest", "Definindo campo address: {}", customer.getAddress());
                try {
                    setField(target, FIELD_ADDRESS, customer.getAddress(), useBuilder);
                    LOGGER.info("populateCustomerRequest", "Campo address definido com sucesso");
                } catch (Exception e) {
                    LOGGER.error("populateCustomerRequest", "Erro ao definir campo address: {}", e.getMessage(), e);
                    throw new GatewayException(Provider.ASAAS, "Erro ao definir campo address: " + e.getMessage(), e);
                }
            }

            if (customer.getAddressNumber() != null) {
                LOGGER.info("populateCustomerRequest", "Definindo campo addressNumber: {}", customer.getAddressNumber());
                try {
                    setField(target, FIELD_ADDRESS_NUMBER, customer.getAddressNumber().toString(), useBuilder);
                    LOGGER.info("populateCustomerRequest", "Campo addressNumber definido com sucesso");
                } catch (Exception e) {
                    LOGGER.error("populateCustomerRequest", "Erro ao definir campo addressNumber: {}", e.getMessage(), e);
                    throw new GatewayException(Provider.ASAAS, "Erro ao definir campo addressNumber: " + e.getMessage(), e);
                }
            }

            if (customer.getComplement() != null && !customer.getComplement().isBlank()) {
                LOGGER.info("populateCustomerRequest", "Definindo campo complement: {}", customer.getComplement());
                try {
                    setField(target, FIELD_COMPLEMENT, customer.getComplement(), useBuilder);
                    LOGGER.info("populateCustomerRequest", "Campo complement definido com sucesso");
                } catch (Exception e) {
                    LOGGER.error("populateCustomerRequest", "Erro ao definir campo complement: {}", e.getMessage(), e);
                    throw new GatewayException(Provider.ASAAS, "Erro ao definir campo complement: " + e.getMessage(), e);
                }
            }

            if (customer.getState() != null && !customer.getState().isBlank()) {
                LOGGER.info("populateCustomerRequest", "Definindo campo province: {}", customer.getState());
                try {
                    setField(target, FIELD_PROVINCE, customer.getState(), useBuilder);
                    LOGGER.info("populateCustomerRequest", "Campo province definido com sucesso");
                } catch (Exception e) {
                    LOGGER.error("populateCustomerRequest", "Erro ao definir campo province: {}", e.getMessage(), e);
                    throw new GatewayException(Provider.ASAAS, "Erro ao definir campo province: " + e.getMessage(), e);
                }
            }

            if (customer.getZipCode() != null && !customer.getZipCode().isBlank()) {
                LOGGER.info("populateCustomerRequest", "Definindo campo postalCode: {}", customer.getZipCode());
                try {
                    setField(target, FIELD_POSTAL_CODE, customer.getZipCode(), useBuilder);
                    LOGGER.info("populateCustomerRequest", "Campo postalCode definido com sucesso");
                } catch (Exception e) {
                    LOGGER.error("populateCustomerRequest", "Erro ao definir campo postalCode: {}", e.getMessage(), e);
                    throw new GatewayException(Provider.ASAAS, "Erro ao definir campo postalCode: " + e.getMessage(), e);
                }
            }

            if (customer.getExternalId() != null && !customer.getExternalId().isBlank()) {
                LOGGER.info("populateCustomerRequest", "Definindo campo externalReference: {}", customer.getExternalId());
                try {
                    setField(target, FIELD_EXTERNAL_REFERENCE, customer.getExternalId(), useBuilder);
                    LOGGER.info("populateCustomerRequest", "Campo externalReference definido com sucesso");
                } catch (Exception e) {
                    LOGGER.error("populateCustomerRequest", "Erro ao definir campo externalReference: {}", e.getMessage(), e);
                    throw new GatewayException(Provider.ASAAS, "Erro ao definir campo externalReference: " + e.getMessage(), e);
                }
            }
        } catch (GatewayException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("populateCustomerRequest", "Erro inesperado ao popular campos do request: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao popular campos do request: " + e.getMessage(), e);
        }
    }

    private void setField(Object target, String fieldName, String value, boolean useBuilder) {
        if (value == null || value.isBlank()) {
            return;
        }
        
        if (useBuilder) {
            invokeBuilderMethod(target, fieldName, value);
        } else {
            ((Map<String, Object>) target).put(fieldName, value);
        }
    }
    
    private void setField(Object target, String fieldName, Object value, boolean useBuilder) {
        if (value == null) {
            return;
        }
        
        if (useBuilder) {
            invokeBuilderMethod(target, fieldName, value);
        } else {
            ((Map<String, Object>) target).put(fieldName, value);
        }
    }

    private String extractCustomerId(Object response) {
        try {
            if (response instanceof Map<?, ?> map) {
                Object id = map.get("id");
                if (id != null) {
                    return id.toString();
                }
            }
            
            Method getIdMethod = response.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(response);
            return id != null ? id.toString() : null;
        } catch (Exception e) {
            LOGGER.warn("extractCustomerId", "Erro ao extrair ID do customer: {}", e.getMessage());
            return null;
        }
    }

    private Object getCustomerService(AsaasSdk sdk) {
        try {
            Field customerField = sdk.getClass().getField("customer");
            customerField.setAccessible(true);
            return customerField.get(sdk);
        } catch (NoSuchFieldException e) {
            try {
                Field customerField = sdk.getClass().getDeclaredField("customer");
                customerField.setAccessible(true);
                return customerField.get(sdk);
            } catch (NoSuchFieldException ex) {
                Field[] allFields = sdk.getClass().getDeclaredFields();
                StringBuilder fieldNames = new StringBuilder();
                for (Field f : allFields) {
                    fieldNames.append(f.getName()).append(", ");
                }
                LOGGER.warn("getCustomerService", 
                        "Campo 'customer' não encontrado. Campos disponíveis: {}", 
                        fieldNames.toString());
                throw new GatewayException(Provider.ASAAS, 
                        "Campo 'customer' não encontrado no AsaasSdk. Campos disponíveis: " + fieldNames.toString(), ex);
            } catch (Exception ex) {
                throw new GatewayException(Provider.ASAAS, "Erro ao acessar campo 'customer'", ex);
            }
        } catch (Exception e) {
            throw new GatewayException(Provider.ASAAS, "Erro ao obter CustomerService", e);
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
                        if (i < params.length - 1) {
                            availableMethods.append(", ");
                        }
                    }
                    availableMethods.append(") - ").append(m.getParameterCount()).append(" params\n");
                    
                    if (m.getParameterCount() == args.length) {
                        try {
                            LOGGER.info("invokeMethod", "Tentando invocar método alternativo: {}", m);
                            Object result = m.invoke(service, args);
                            LOGGER.info("invokeMethod", "Método alternativo invocado com sucesso");
                            return result;
                        } catch (Exception ex) {
                            LOGGER.error("invokeMethod", "Erro ao invocar método alternativo {}: {}", m.getName(), ex.getMessage(), ex);
                        }
                    }
                }
            }
            
            if (availableMethods.length() > 0) {
                LOGGER.error("invokeMethod", "Métodos disponíveis com nome {}:\n{}", methodName, availableMethods.toString());
            } else {
                LOGGER.error("invokeMethod", "Nenhum método com nome {} encontrado na classe {}", methodName, service.getClass().getName());
            }
            
            throw new GatewayException(Provider.ASAAS, "Método " + methodName + " não encontrado. Métodos disponíveis: " + availableMethods.toString(), e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            LOGGER.error("invokeMethod", "Erro ao invocar método {}: {}", methodName, cause != null ? cause.getMessage() : e.getMessage(), cause != null ? cause : e);
            throw new GatewayException(Provider.ASAAS, "Erro ao invocar método " + methodName + ": " + (cause != null ? cause.getMessage() : e.getMessage()), cause != null ? cause : e);
        } catch (Exception e) {
            LOGGER.error("invokeMethod", "Erro inesperado ao invocar método {}: {}", methodName, e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao invocar método " + methodName + ": " + e.getMessage(), e);
        }
    }

    private void invokeBuilderMethod(Object builder, String fieldName, Object value) {
        if (value == null) {
            return;
        }
        
        String methodName = fieldName;
        try {
            Class<?> valueClass = value.getClass();
            Method method = findMethod(builder.getClass(), methodName, valueClass);
            if (method != null) {
                method.invoke(builder, value);
                LOGGER.info("invokeBuilderMethod", "Método {} invocado com sucesso usando tipo {}", methodName, valueClass.getSimpleName());
                return;
            }
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            String errorMsg = cause != null ? cause.getMessage() : e.getMessage();
            LOGGER.error("invokeBuilderMethod", "Erro ao invocar {} com tipo exato {}: {}", methodName, value.getClass().getSimpleName(), errorMsg, cause != null ? cause : e);
            throw new GatewayException(Provider.ASAAS, "Erro ao definir campo " + fieldName + ": " + errorMsg, cause != null ? cause : e);
        } catch (Exception e) {
            LOGGER.info("invokeBuilderMethod", "Tentativa com tipo exato falhou para {}: {}", methodName, e.getMessage());
        }
        
        try {
            Method method = findMethod(builder.getClass(), methodName, String.class);
            if (method != null) {
                String stringValue = value.toString();
                method.invoke(builder, stringValue);
                LOGGER.info("invokeBuilderMethod", "Método {} invocado com sucesso convertendo para String", methodName);
                return;
            }
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            String errorMsg = cause != null ? cause.getMessage() : e.getMessage();
            LOGGER.error("invokeBuilderMethod", "Erro ao invocar {} com String: {}", methodName, errorMsg, cause != null ? cause : e);
            throw new GatewayException(Provider.ASAAS, "Erro ao definir campo " + fieldName + ": " + errorMsg, cause != null ? cause : e);
        } catch (Exception e) {
            LOGGER.info("invokeBuilderMethod", "Tentativa com String falhou para {}: {}", methodName, e.getMessage());
        }
        
        Method[] methods = builder.getClass().getMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName) && m.getParameterCount() == 1) {
                Class<?> paramType = m.getParameterTypes()[0];
                try {
                    if (paramType.isAssignableFrom(value.getClass())) {
                        m.invoke(builder, value);
                        LOGGER.info("invokeBuilderMethod", "Método {} invocado com sucesso usando tipo compatível {}", methodName, paramType.getSimpleName());
                        return;
                    } else if (paramType == String.class) {
                        m.invoke(builder, value.toString());
                        LOGGER.info("invokeBuilderMethod", "Método {} invocado com sucesso convertendo para String", methodName);
                        return;
                    }
                } catch (java.lang.reflect.InvocationTargetException ex) {
                    Throwable cause = ex.getCause();
                    String errorMsg = cause != null ? cause.getMessage() : ex.getMessage();
                    LOGGER.error("invokeBuilderMethod", "Erro ao invocar {} com tipo {}: {}", methodName, paramType.getSimpleName(), errorMsg, cause != null ? cause : ex);
                    throw new GatewayException(Provider.ASAAS, "Erro ao definir campo " + fieldName + ": " + errorMsg, cause != null ? cause : ex);
                } catch (Exception ex) {
                    LOGGER.info("invokeBuilderMethod", "Tentativa de invocar {} com tipo {} falhou: {}", methodName, paramType.getSimpleName(), ex.getMessage());
                    continue;
                }
            }
        }
        
        LOGGER.warn("invokeBuilderMethod", "Não foi possível encontrar ou invocar método {} para campo {} com valor do tipo {}", 
                methodName, fieldName, value.getClass().getSimpleName());
    }
    
    private Method findMethod(Class<?> clazz, String methodName, Class<?> paramType) {
        try {
            return clazz.getMethod(methodName, paramType);
        } catch (NoSuchMethodException e) {
            Method[] methods = clazz.getMethods();
            for (Method m : methods) {
                if (m.getName().equals(methodName) && m.getParameterCount() == 1) {
                    Class<?> mParamType = m.getParameterTypes()[0];
                    if (mParamType.equals(paramType) || mParamType.isAssignableFrom(paramType)) {
                        return m;
                    }
                }
            }
            return null;
        }
    }

    private Object createListCustomersParameters(String name, String email, String cpfCnpj, String groupName, String externalReference, Integer offset, Integer limit) {
        try {
            LOGGER.info("createListCustomersParameters", "Criando ListCustomersParameters via reflection");
            Class<?> listParametersClass = Class.forName("com.asaas.apisdk.models.ListCustomersParameters");
            LOGGER.info("createListCustomersParameters", "Classe ListCustomersParameters carregada: {}", listParametersClass.getName());
            
            Method builderMethod = listParametersClass.getMethod("builder");
            Object builder = builderMethod.invoke(null);
            LOGGER.info("createListCustomersParameters", "Builder criado com sucesso");
            
            if (name != null && !name.isBlank()) {
                invokeBuilderMethod(builder, "name", name);
            }
            if (email != null && !email.isBlank()) {
                invokeBuilderMethod(builder, "email", email);
            }
            if (cpfCnpj != null && !cpfCnpj.isBlank()) {
                invokeBuilderMethod(builder, "cpfCnpj", cpfCnpj);
            }
            if (groupName != null && !groupName.isBlank()) {
                invokeBuilderMethod(builder, "groupName", groupName);
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
            LOGGER.info("createListCustomersParameters", "ListCustomersParameters criado com sucesso");
            return parameters;
        } catch (Exception e) {
            LOGGER.error("createListCustomersParameters", "Erro ao criar ListCustomersParameters: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao criar parâmetros de listagem: " + e.getMessage(), e);
        }
    }

    private List<Object> extractCustomerList(Object response) {
        try {
            List<Object> customerList = new ArrayList<>();
            
            Object data = extractFieldObject(response, "data");
            if (data == null) {
                data = extractFieldObject(response, "customers");
            }
            if (data == null) {
                data = response;
            }
            
            if (data instanceof List) {
                List<?> dataList = (List<?>) data;
                LOGGER.info("extractCustomerList", "Extraindo {} customers da lista", dataList.size());
                for (Object item : dataList) {
                    customerList.add(item);
                }
            } else {
                LOGGER.warn("extractCustomerList", "Resposta não é uma lista, adicionando como objeto único");
                customerList.add(data);
            }
            
            LOGGER.info("extractCustomerList", "Extração concluída: {} customers", customerList.size());
            return customerList;
        } catch (Exception e) {
            LOGGER.error("extractCustomerList", "Erro ao extrair lista de customers: {}", e.getMessage(), e);
            throw new GatewayException(Provider.ASAAS, "Erro ao extrair lista de customers: " + e.getMessage(), e);
        }
    }

    private Object extractFieldObject(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException e) {
            try {
                Method method = obj.getClass().getMethod("get" + capitalize(fieldName));
                return method.invoke(obj);
            } catch (Exception ex) {
                return null;
            }
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
}

