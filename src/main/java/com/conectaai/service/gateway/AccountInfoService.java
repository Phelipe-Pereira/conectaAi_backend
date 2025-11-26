package com.conectaai.service.gateway;

import com.asaas.apisdk.AsaasSdk;
import com.conectaai.exception.GatewayException;
import com.conectaai.logger.AppLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountInfoService {

    private static final AppLogger LOGGER = AppLogger.getLogger(AccountInfoService.class);
    private final AsaasSdkFactory asaasSdkFactory;
    private final GatewayConfigService gatewayConfigService;

    private AsaasSdk getAsaasSdkForUser(Long userId) {
        String apiKey = gatewayConfigService.getAsaasApiKey(userId);
        if (apiKey == null) {
            throw new GatewayException(com.conectaai.enums.Provider.ASAAS,
                "Chave do Asaas não configurada. Configure sua chave nas configurações do gateway.");
        }
        return asaasSdkFactory.createSdkForUser(apiKey);
    }

    private Object getAccountInfoService(AsaasSdk sdk) {
        try {
            java.lang.reflect.Field accountInfoField = sdk.getClass().getDeclaredField("accountInfo");
            accountInfoField.setAccessible(true);
            return accountInfoField.get(sdk);
        } catch (Exception e) {
            LOGGER.error("getAccountInfoService", "Erro ao obter AccountInfoService do AsaasSdk", e);
            throw new GatewayException(com.conectaai.enums.Provider.ASAAS,
                "Erro ao acessar AccountInfoService: " + e.getMessage(), e);
        }
    }

    private Object invokeMethod(Object service, String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
            }
            Method method = service.getClass().getMethod(methodName, paramTypes);
            return method.invoke(service, args);
        } catch (Exception e) {
            LOGGER.error("invokeMethod", "Erro ao invocar método {}: {}", methodName, e.getMessage(), e);
            throw new GatewayException(com.conectaai.enums.Provider.ASAAS,
                "Erro ao chamar método " + methodName + ": " + e.getMessage(), e);
        }
    }

    private Map<String, Object> convertResponseToMap(Object response) {
        Map<String, Object> result = new HashMap<>();
        try {
            for (java.lang.reflect.Field field : response.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(response);
                result.put(field.getName(), value);
            }
        } catch (Exception e) {
            LOGGER.warn("convertResponseToMap", "Erro ao converter resposta para Map: {}", e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getBusinessData(Long userId) {
        try {
            AsaasSdk sdk = getAsaasSdkForUser(userId);
            Object accountInfoService = getAccountInfoService(sdk);
            Object response = invokeMethod(accountInfoService, "retrieveBusinessData");
            return convertResponseToMap(response);
        } catch (Exception e) {
            LOGGER.error("getBusinessData", "Erro ao buscar dados comerciais: {}", e.getMessage(), e);
            throw new GatewayException(com.conectaai.enums.Provider.ASAAS,
                "Erro ao buscar dados comerciais: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getAccountNumber(Long userId) {
        try {
            AsaasSdk sdk = getAsaasSdkForUser(userId);
            Object accountInfoService = getAccountInfoService(sdk);
            Object response = invokeMethod(accountInfoService, "retrieveAsaasAccountNumber");
            return convertResponseToMap(response);
        } catch (Exception e) {
            LOGGER.error("getAccountNumber", "Erro ao buscar número da conta: {}", e.getMessage(), e);
            throw new GatewayException(com.conectaai.enums.Provider.ASAAS,
                "Erro ao buscar número da conta: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getAccountFees(Long userId) {
        try {
            AsaasSdk sdk = getAsaasSdkForUser(userId);
            Object accountInfoService = getAccountInfoService(sdk);
            Object response = invokeMethod(accountInfoService, "retrieveAccountFees");
            return convertResponseToMap(response);
        } catch (Exception e) {
            LOGGER.error("getAccountFees", "Erro ao buscar taxas: {}", e.getMessage(), e);
            throw new GatewayException(com.conectaai.enums.Provider.ASAAS,
                "Erro ao buscar taxas: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getAccountStatus(Long userId) {
        try {
            AsaasSdk sdk = getAsaasSdkForUser(userId);
            Object accountInfoService = getAccountInfoService(sdk);
            Object response = invokeMethod(accountInfoService, "checkAccountRegistrationStatus");
            return convertResponseToMap(response);
        } catch (Exception e) {
            LOGGER.error("getAccountStatus", "Erro ao buscar status: {}", e.getMessage(), e);
            throw new GatewayException(com.conectaai.enums.Provider.ASAAS,
                "Erro ao buscar status: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getWalletId(Long userId) {
        try {
            AsaasSdk sdk = getAsaasSdkForUser(userId);
            Object accountInfoService = getAccountInfoService(sdk);
            Object response = invokeMethod(accountInfoService, "retrieveWalletid");
            return convertResponseToMap(response);
        } catch (Exception e) {
            LOGGER.error("getWalletId", "Erro ao buscar wallet ID: {}", e.getMessage(), e);
            throw new GatewayException(com.conectaai.enums.Provider.ASAAS,
                "Erro ao buscar wallet ID: " + e.getMessage(), e);
        }
    }
}

