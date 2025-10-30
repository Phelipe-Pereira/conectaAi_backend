package com.conectaai.service.refund;

import com.conectaai.domain.Payment;
import com.conectaai.domain.Refund;
import com.conectaai.dto.refund.RefundMapper;
import com.conectaai.dto.refund.RefundRequestDto;
import com.conectaai.dto.refund.RefundResponseDto;
import com.conectaai.dto.refund.RefundSummaryDto;
import com.conectaai.dto.refund.RefundUpdateDto;
import com.conectaai.enums.RefundStatus;
import com.conectaai.exception.InsufficientRefundableAmountException;
import com.conectaai.exception.InvalidRefundStatusTransitionException;
import com.conectaai.exception.PaymentNotRefundableException;
import com.conectaai.exception.RefundNotFoundException;
import com.conectaai.logger.AppLogger;
import com.conectaai.repository.PaymentRepository;
import com.conectaai.repository.RefundRepository;
import com.conectaai.specification.RefundSpecification;
import com.conectaai.utils.RefundUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefundService {

    private static final AppLogger LOGGER = AppLogger.getLogger(RefundService.class);

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public RefundResponseDto createRefund(RefundRequestDto refundRequest) {
        LOGGER.info("createRefund", "Iniciando criação de reembolso para payment {}", refundRequest.paymentId());

        Payment payment = findPaymentByIdOrThrow(refundRequest.paymentId());

        validateRefundRequest(refundRequest, payment);

        Refund refund = RefundMapper.toEntity(refundRequest, payment);
        Refund savedRefund = refundRepository.save(refund);

        LOGGER.info("createRefund", "Reembolso criado com sucesso. ID: {}", savedRefund.getId());
        return RefundMapper.toResponseDto(savedRefund);
    }

    @Transactional(readOnly = true)
    public Page<RefundSummaryDto> findRefunds(
            String externalId,
            Long paymentId,
            RefundStatus status,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDateTime createdStart,
            LocalDateTime createdEnd,
            LocalDateTime processedStart,
            LocalDateTime processedEnd,
            Pageable pageable
    ) {
        LOGGER.info("findRefunds", "Buscando reembolsos com filtros aplicados");

        Specification<Refund> spec = RefundSpecification.hasExternalId(externalId)
                .and(RefundSpecification.hasPaymentId(paymentId))
                .and(RefundSpecification.hasStatus(status))
                .and(RefundSpecification.amountBetween(minAmount, maxAmount))
                .and(RefundSpecification.createdBetween(createdStart, createdEnd))
                .and(RefundSpecification.processedBetween(processedStart, processedEnd));

        return refundRepository.findAll(spec, pageable)
                .map(RefundMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public RefundResponseDto findById(Long id) {
        LOGGER.info("findById", "Buscando reembolso por ID: {}", id);
        Refund refund = findByIdOrThrow(id);
        return RefundMapper.toResponseDto(refund);
    }

    @Transactional(readOnly = true)
    public RefundResponseDto findByExternalId(String externalId) {
        LOGGER.info("findByExternalId", "Buscando reembolso por External ID: {}", externalId);
        Refund refund = refundRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RefundNotFoundException("Reembolso não encontrado: " + externalId));
        return RefundMapper.toResponseDto(refund);
    }

    @Transactional
    public RefundResponseDto updateStatus(Long id, RefundUpdateDto updateRequest) {
        LOGGER.info("updateStatus", "Atualizando status do reembolso ID: {}", id);
        Refund refund = findByIdOrThrow(id);

        if (updateRequest.status() != null) {
            validateStatusTransition(refund.getStatus(), updateRequest.status());
            refund.setStatus(updateRequest.status());
        }

        RefundMapper.updateEntity(refund, updateRequest);
        Refund updatedRefund = refundRepository.save(refund);

        LOGGER.info("updateStatus", "Status do reembolso ID {} atualizado para {}", id, updatedRefund.getStatus());
        return RefundMapper.toResponseDto(updatedRefund);
    }

    @Transactional
    public void cancelRefund(Long id) {
        LOGGER.info("cancelRefund", "Cancelando reembolso ID: {}", id);
        Refund refund = findByIdOrThrow(id);

        if (!RefundUtils.isCancellable(refund.getStatus())) {
            throw new IllegalArgumentException("Reembolso com status " + refund.getStatus() + " não pode ser cancelado.");
        }

        refund.setStatus(RefundStatus.CANCELLED);
        refundRepository.save(refund);
        LOGGER.info("cancelRefund", "Reembolso ID {} cancelado com sucesso.", id);
    }

    private Refund findByIdOrThrow(Long id) {
        return refundRepository.findById(id)
                .orElseThrow(() -> new RefundNotFoundException("Reembolso não encontrado: " + id));
    }

    private Payment findPaymentByIdOrThrow(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Pagamento não encontrado: " + paymentId));
    }

    private void validateRefundRequest(RefundRequestDto request, Payment payment) {
        if (!RefundUtils.isValidAmount(request.amount())) {
            throw new IllegalArgumentException("Valor do reembolso deve ser maior que zero");
        }

        if (!RefundUtils.isPaymentRefundable(payment.getStatus())) {
            throw new PaymentNotRefundableException("Pagamento com status " + payment.getStatus() + " não pode ser reembolsado");
        }

        BigDecimal alreadyRefunded = refundRepository.sumRefundedAmountByPaymentId(payment.getId());
        if (alreadyRefunded == null) {
            alreadyRefunded = BigDecimal.ZERO;
        }

        BigDecimal availableAmount = payment.getAmount().subtract(alreadyRefunded);

        if (request.amount().compareTo(availableAmount) > 0) {
            throw new InsufficientRefundableAmountException(
                    String.format("Valor do reembolso (R$ %.2f) excede o valor disponível (R$ %.2f)",
                            request.amount(), availableAmount)
            );
        }
    }

    private void validateStatusTransition(RefundStatus currentStatus, RefundStatus newStatus) {
        if (!RefundUtils.isValidStatusTransition(currentStatus, newStatus)) {
            throw new InvalidRefundStatusTransitionException("Transição de status inválida de " + currentStatus + " para " + newStatus);
        }
    }
}

