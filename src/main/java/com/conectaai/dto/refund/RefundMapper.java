package com.conectaai.dto.refund;

import com.conectaai.domain.Payment;
import com.conectaai.domain.Refund;
import com.conectaai.enums.RefundStatus;

public class RefundMapper {

    private RefundMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Refund toEntity(RefundRequestDto refundRequest, Payment payment) {
        return Refund.builder()
                .payment(payment)
                .amount(refundRequest.amount())
                .status(RefundStatus.PENDING)
                .reason(refundRequest.reason())
                .build();
    }

    public static RefundResponseDto toResponseDto(Refund refund) {
        return new RefundResponseDto(
                refund.getId().toString(),
                refund.getPayment().getId().toString(),
                refund.getAmount(),
                refund.getStatus(),
                refund.getReason(),
                refund.getProviderReference(),
                refund.getCreatedAt()
        );
    }
}

