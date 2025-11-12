package com.conectaai.config.gateway;

import com.stripe.Stripe;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class StripeSdkConfig {

    @Value("${STRIPE_SECRET:}")
    private String stripeSecretKey;

    @PostConstruct
    public void configureStripe() {
        if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
            Stripe.apiKey = stripeSecretKey;
        }
    }
}

