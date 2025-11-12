package com.conectaai.adapter.gateway;

import com.conectaai.domain.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SubscriptionGatewayAdapter {

    GatewaySubscriptionResponse createSubscription(
            Customer customer,
            BigDecimal amount,
            String currency,
            String interval,
            String paymentMethod,
            String description,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String externalId
    );

    GatewaySubscriptionResponse getSubscription(String providerSubscriptionId);

    void cancelSubscription(String providerSubscriptionId);
}

