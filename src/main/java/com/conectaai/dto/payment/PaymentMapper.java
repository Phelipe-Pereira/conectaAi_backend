package com.conectaai.dto.payment;

import com.conectaai.domain.Customer;
import com.conectaai.domain.Payment;
import com.conectaai.enums.PaymentStatus;

import java.util.UUID;

public class PaymentMapper {

    private PaymentMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Payment toEntity(PaymentRequestDto paymentRequest, Customer customer) {
        return Payment.builder()
                .publicId(UUID.randomUUID().toString())
                .customer(customer)
                .amount(paymentRequest.amount())
                .currency(paymentRequest.currency())
                .status(PaymentStatus.PENDING)
                .description(paymentRequest.description())
                .paymentMethod(paymentRequest.paymentMethod())
                .dueDate(paymentRequest.dueDate())
                .build();
    }

    public static PaymentResponseDto toResponseDto(Payment payment) {
        return new PaymentResponseDto(
                payment.getId().toString(),
                payment.getPublicId(),
                payment.getCustomer().getId().toString(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getDescription(),
                payment.getPaymentMethod(),
                payment.getDueDate(),
                payment.getProvider(),
                payment.getProviderReference(),
                null,
                null,
                null,
                payment.getCreatedAt()
        );
    }

    public static PaymentResponseDto toResponseDto(Payment payment, String paymentUrl, String barcode, String pixQrcode) {
        return new PaymentResponseDto(
                payment.getId().toString(),
                payment.getPublicId(),
                payment.getCustomer().getId().toString(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getDescription(),
                payment.getPaymentMethod(),
                payment.getDueDate(),
                payment.getProvider(),
                payment.getProviderReference(),
                paymentUrl,
                barcode,
                pixQrcode,
                payment.getCreatedAt()
        );
    }
}

