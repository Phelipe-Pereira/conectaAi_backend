package com.conectaai.service.gateway;

import com.asaas.apisdk.AsaasSdk;
import com.asaas.apisdk.config.ApiKeyAuthConfig;
import com.asaas.apisdk.config.AsaasSdkConfig;
import com.asaas.apisdk.http.Environment;
import com.conectaai.exception.GatewayException;
import com.conectaai.logger.AppLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsaasSdkFactory {

    private static final AppLogger LOGGER = AppLogger.getLogger(AsaasSdkFactory.class);

    public AsaasSdk createSdkForUser(String userAsaasApiKey) {
        if (userAsaasApiKey == null || userAsaasApiKey.isBlank()) {
            LOGGER.warn("createSdkForUser", "Chave do Asaas não configurada para o usuário");
            throw new GatewayException(com.conectaai.enums.Provider.ASAAS, 
                "Chave do Asaas não configurada. Configure sua chave nas configurações do gateway.");
        }

        try {
            ApiKeyAuthConfig authConfig = ApiKeyAuthConfig.builder()
                    .apiKey(userAsaasApiKey)
                    .build();

            AsaasSdkConfig config = AsaasSdkConfig.builder()
                    .apiKeyAuthConfig(authConfig)
                    .build();

            AsaasSdk sdk = new AsaasSdk(config);
            sdk.setEnvironment(Environment.SANDBOX);
            
            LOGGER.info("createSdkForUser", "Instância do AsaasSdk criada com sucesso para o usuário");
            return sdk;
        } catch (Exception e) {
            LOGGER.error("createSdkForUser", "Erro ao criar instância do AsaasSdk: {}", e.getMessage(), e);
            throw new GatewayException(com.conectaai.enums.Provider.ASAAS, 
                "Erro ao configurar SDK do Asaas: " + e.getMessage(), e);
        }
    }
}

