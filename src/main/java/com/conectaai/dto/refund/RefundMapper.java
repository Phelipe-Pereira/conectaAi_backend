package com.conectaai.dto.refund;

import com.conectaai.domain.Payment;
import com.conectaai.domain.Refund;
import com.conectaai.dto.payment.PaymentMapper;
import com.conectaai.enums.RefundStatus;

import java.util.UUID;

public final class RefundMapper {

    private RefundMapper() {
    }

    public static Refund toEntity(RefundRequestDto refundRequest, Payment payment) {
        return Refund.builder()
                .externalId(generateExternalId())
                .payment(payment)
                .amount(refundRequest.amount())
                .reason(refundRequest.reason())
                .status(RefundStatus.PENDING)
                .build();
    }

    public static RefundResponseDto toResponseDto(Refund refund) {
        return new RefundResponseDto(
                refund.getId(),
                refund.getExternalId(),
                refund.getProviderRefundId(),
                PaymentMapper.toSummaryDto(refund.getPayment()),
                refund.getAmount(),
                refund.getStatus(),
                refund.getReason(),
                refund.getProcessedAt(),
                refund.getCreatedAt(),
                refund.getUpdatedAt()
        );
    }

    public static RefundSummaryDto toSummaryDto(Refund refund) {
        return new RefundSummaryDto(
                refund.getId(),
                refund.getExternalId(),
                PaymentMapper.toSummaryDto(refund.getPayment()),
                refund.getAmount(),
                refund.getStatus(),
                refund.getCreatedAt()
        );
    }

    public static void updateEntity(Refund refund, RefundUpdateDto updateRequest) {
        if (updateRequest.status() != null) {
            refund.setStatus(updateRequest.status());
        }
        if (updateRequest.providerRefundId() != null) {
            refund.setProviderRefundId(updateRequest.providerRefundId());
        }
        if (updateRequest.processedAt() != null) {
            refund.setProcessedAt(updateRequest.processedAt());
        }
    }

    private static String generateExternalId() {
        return "ref_" + UUID.randomUUID().toString().replace("-", "");
    }
}
