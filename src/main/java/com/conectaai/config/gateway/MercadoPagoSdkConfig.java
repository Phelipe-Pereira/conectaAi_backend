package com.conectaai.config.gateway;

import com.mercadopago.MercadoPagoConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class MercadoPagoSdkConfig {

    @Value("${MP_ACCESS_TOKEN:}")
    private String mpAccessToken;

    @PostConstruct
    public void configureMercadoPago() {
        if (mpAccessToken != null && !mpAccessToken.isBlank()) {
            MercadoPagoConfig.setAccessToken(mpAccessToken);
        }
    }
}

