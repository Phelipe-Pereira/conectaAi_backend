package com.conectaai.adapter.gateway;

import com.conectaai.domain.Customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    List<GatewayPaymentResponse> listPayments(String customerId, Integer offset, Integer limit);

    GatewayPaymentResponse getPayment(String providerPaymentId);

    void cancelPayment(String providerPaymentId);

    GatewayPaymentResponse restorePayment(String providerPaymentId);
}

