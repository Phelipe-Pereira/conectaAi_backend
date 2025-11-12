package com.conectaai.adapter.gateway;

import com.conectaai.domain.Customer;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PaymentGatewayAdapter {

    GatewayPaymentResponse createPayment(
            Customer customer,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            String description,
            LocalDate dueDate,
            String externalId
    );

    GatewayPaymentResponse getPayment(String providerPaymentId);

    void cancelPayment(String providerPaymentId);
}

