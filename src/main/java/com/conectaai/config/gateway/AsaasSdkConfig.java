package com.conectaai.config.gateway;

import com.asaas.apisdk.AsaasSdk;
import com.asaas.apisdk.config.ApiKeyAuthConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsaasSdkConfig {

    @Value("${ASAAS_TOKEN:}")
    private String asaasToken;

    @Bean
    public AsaasSdk asaasSdk() {
        ApiKeyAuthConfig authConfig = ApiKeyAuthConfig.builder()
                .apiKey(asaasToken)
                .build();

        com.asaas.apisdk.config.AsaasSdkConfig config = com.asaas.apisdk.config.AsaasSdkConfig.builder()
                .apiKeyAuthConfig(authConfig)
                .build();

        return new AsaasSdk(config);
    }
}

