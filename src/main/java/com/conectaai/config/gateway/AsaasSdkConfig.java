package com.conectaai.config.gateway;

import com.asaas.apisdk.AsaasSdk;
import com.asaas.apisdk.config.ApiKeyAuthConfig;
import com.asaas.apisdk.http.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsaasSdkConfig {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AsaasSdkConfig.class);

    @Value("${ASAAS_TOKEN:}")
    private String asaasToken;

    @Bean
    public AsaasSdk asaasSdk() {
        if (asaasToken == null || asaasToken.isBlank()) {
            log.warn("ASAAS_TOKEN não configurado ou vazio!");
        } else {
            log.info("AsaasSdk configurado com token (primeiros 10 chars): {}", 
                    asaasToken.length() > 10 ? asaasToken.substring(0, 10) + "..." : "***");
            log.info("Tamanho total do token: {} caracteres", asaasToken.length());
        }
        
        ApiKeyAuthConfig authConfig = ApiKeyAuthConfig.builder()
                .apiKey(asaasToken)
                .build();

        com.asaas.apisdk.config.AsaasSdkConfig config = com.asaas.apisdk.config.AsaasSdkConfig.builder()
                .apiKeyAuthConfig(authConfig)
                .build();

        AsaasSdk sdk = new AsaasSdk(config);
        sdk.setEnvironment(Environment.SANDBOX);
        log.info("AsaasSdk configurado para usar ambiente: SANDBOX (https://api-sandbox.asaas.com/)");
        
        return sdk;
    }
}

