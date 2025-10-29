package com.conectaai.dto.payment;

import com.conectaai.domain.Customer;
import com.conectaai.domain.Payment;
import com.conectaai.dto.customer.CustomerSummaryDto;

import java.util.UUID;

public final class PaymentMapper {

    private PaymentMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Payment toEntity(PaymentRequestDto paymentRequest, Customer customer) {
        return Payment.builder()
                .externalId(generateExternalId())
                .provider(paymentRequest.provider())
                .customer(customer)
                .amount(paymentRequest.amount())
                .currency(paymentRequest.currency())
                .paymentMethod(paymentRequest.paymentMethod())
                .description(paymentRequest.description())
                .dueDate(paymentRequest.dueDate())
                .build();
    }

    public static PaymentResponseDto toResponseDto(Payment payment) {
        return new PaymentResponseDto(
                payment.getId(),
                payment.getExternalId(),
                payment.getProviderPaymentId(),
                payment.getProvider(),
                toCustomerSummary(payment.getCustomer()),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getDescription(),
                payment.getDueDate(),
                payment.getPaidAt(),
                payment.getPaymentUrl(),
                payment.getQrCode(),
                payment.getBarCode(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    public static PaymentSummaryDto toSummaryDto(Payment payment) {
        return new PaymentSummaryDto(
                payment.getId(),
                payment.getExternalId(),
                payment.getProvider(),
                toCustomerSummary(payment.getCustomer()),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getDueDate(),
                payment.getPaidAt(),
                payment.getPaymentUrl(),
                payment.getCreatedAt()
        );
    }

    public static void updateEntity(Payment payment, PaymentUpdateDto updateRequest) {
        if (updateRequest.status() != null) {
            payment.setStatus(updateRequest.status());
        }
        if (updateRequest.providerPaymentId() != null) {
            payment.setProviderPaymentId(updateRequest.providerPaymentId());
        }
        if (updateRequest.paidAt() != null) {
            payment.setPaidAt(updateRequest.paidAt());
        }
        if (updateRequest.paymentUrl() != null) {
            payment.setPaymentUrl(updateRequest.paymentUrl());
        }
        if (updateRequest.qrCode() != null) {
            payment.setQrCode(updateRequest.qrCode());
        }
        if (updateRequest.barCode() != null) {
            payment.setBarCode(updateRequest.barCode());
        }
    }

    private static CustomerSummaryDto toCustomerSummary(Customer customer) {
        if (customer == null) {
            return null;
        }
        return new CustomerSummaryDto(
                customer.getId(),
                customer.getExternalId(),
                customer.getFullName(),
                customer.getEmail()
        );
    }

    private static String generateExternalId() {
        return "pay_" + UUID.randomUUID().toString().replace("-", "");
    }
}
