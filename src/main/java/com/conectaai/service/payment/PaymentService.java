package com.conectaai.service.payment;

import com.conectaai.domain.Customer;
import com.conectaai.domain.Payment;
import com.conectaai.dto.payment.PaymentMapper;
import com.conectaai.dto.payment.PaymentRequestDto;
import com.conectaai.dto.payment.PaymentResponseDto;
import com.conectaai.dto.payment.PaymentSummaryDto;
import com.conectaai.dto.payment.PaymentUpdateDto;
import com.conectaai.enums.PaymentStatus;
import com.conectaai.enums.Provider;
import com.conectaai.exception.*;
import com.conectaai.logger.AppLogger;
import com.conectaai.repository.PaymentRepository;
import com.conectaai.service.customer.CustomerService;
import com.conectaai.specification.PaymentSpecification;
import com.conectaai.utils.PaymentUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final AppLogger LOGGER = AppLogger.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final CustomerService customerService;

    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDto paymentRequest) {
        LOGGER.info("createPayment", "Criando pagamento para o cliente ID: {}", paymentRequest.customerId());

        validatePaymentRequest(paymentRequest);

        Customer customer = customerService.findCustomerEntityById(paymentRequest.customerId());

        Payment payment = PaymentMapper.toEntity(paymentRequest, customer);

        validateExternalIdUniqueness(payment.getExternalId());

        Payment savedPayment = paymentRepository.save(payment);
        LOGGER.info("createPayment", "Pagamento criado com sucesso. ID: {}, ExternalID: {}", savedPayment.getId(), savedPayment.getExternalId());

        return PaymentMapper.toResponseDto(savedPayment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentSummaryDto> findPayments(
            String externalId,
            Long customerId,
            Provider provider,
            PaymentStatus status,
            String paymentMethod,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDateTime createdStart,
            LocalDateTime createdEnd,
            LocalDate dueStart,
            LocalDate dueEnd,
            Pageable pageable
    ) {
        LOGGER.info("findPayments", "Buscando pagamentos com filtros aplicados");

        Specification<Payment> spec = PaymentSpecification.hasExternalId(externalId)
                .and(PaymentSpecification.hasCustomerId(customerId))
                .and(PaymentSpecification.hasProvider(provider))
                .and(PaymentSpecification.hasStatus(status))
                .and(PaymentSpecification.hasPaymentMethod(paymentMethod))
                .and(PaymentSpecification.amountBetween(minAmount, maxAmount))
                .and(PaymentSpecification.createdBetween(createdStart, createdEnd))
                .and(PaymentSpecification.dueDateBetween(dueStart, dueEnd));

        return paymentRepository.findAll(spec, pageable)
                .map(PaymentMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto findById(Long id) {
        LOGGER.info("findById", "Buscando pagamento por ID: {}", id);
        Payment payment = findByIdOrThrow(id);
        return PaymentMapper.toResponseDto(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto findByExternalId(String externalId) {
        LOGGER.info("findByExternalId", "Buscando pagamento por ExternalID: {}", externalId);
        Payment payment = paymentRepository.findByExternalId(externalId)
                .orElseThrow(() -> new PaymentNotFoundException("Pagamento não encontrado: " + externalId));
        return PaymentMapper.toResponseDto(payment);
    }

    @Transactional
    public PaymentResponseDto updateStatus(Long id, PaymentUpdateDto updateRequest) {
        LOGGER.info("updateStatus", "Atualizando status do pagamento ID: {}", id);

        Payment payment = findByIdOrThrow(id);

        if (updateRequest.status() != null) {
            validateStatusTransition(payment.getStatus(), updateRequest.status());
        }

        PaymentMapper.updateEntity(payment, updateRequest);

        Payment updatedPayment = paymentRepository.save(payment);
        LOGGER.info("updateStatus", "Status do pagamento atualizado com sucesso. ID: {}, Novo Status: {}", updatedPayment.getId(), updatedPayment.getStatus());

        return PaymentMapper.toResponseDto(updatedPayment);
    }

    @Transactional
    public void cancelPayment(Long id) {
        LOGGER.info("cancelPayment", "Cancelando pagamento ID: {}", id);

        Payment payment = findByIdOrThrow(id);

        if (!PaymentUtils.isCancellable(payment.getStatus())) {
            throw new InvalidPaymentStatusTransitionException(
                    String.format("Pagamento com status %s não pode ser cancelado", payment.getStatus())
            );
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        LOGGER.info("cancelPayment", "Pagamento cancelado com sucesso. ID: {}", id);
    }

    private Payment findByIdOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Pagamento não encontrado: " + id));
    }

    private void validatePaymentRequest(PaymentRequestDto paymentRequest) {
        if (!PaymentUtils.meetsMinimumAmount(paymentRequest.amount())) {
            throw new InvalidAmountException("Valor mínimo do pagamento é R$ 5,00");
        }

        if (!PaymentUtils.isValidDueDate(paymentRequest.dueDate())) {
            throw new InvalidDueDateException("Data de vencimento deve ser no futuro");
        }

        if (!PaymentUtils.isPaymentMethodSupported(paymentRequest.provider(), paymentRequest.paymentMethod())) {
            throw new UnsupportedPaymentMethodException(
                    String.format("Método de pagamento %s não suportado pelo provedor %s",
                            paymentRequest.paymentMethod(), paymentRequest.provider())
            );
        }

        if (!PaymentUtils.isCurrencySupported(paymentRequest.provider(), paymentRequest.currency())) {
            throw new UnsupportedCurrencyException(
                    String.format("Moeda %s não suportada pelo provedor %s",
                            paymentRequest.currency(), paymentRequest.provider())
            );
        }
    }

    private void validateExternalIdUniqueness(String externalId) {
        if (paymentRepository.existsByExternalId(externalId)) {
            throw new PaymentAlreadyExistsException("Pagamento já existe com este ID: " + externalId);
        }
    }

    private void validateStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        if (!PaymentUtils.isValidStatusTransition(currentStatus, newStatus)) {
            throw new InvalidPaymentStatusTransitionException(
                    String.format("Transição de status inválida: %s -> %s", currentStatus, newStatus)
            );
        }
    }
}

