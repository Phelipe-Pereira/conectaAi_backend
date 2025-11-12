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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final String METHOD_CREATE_PAYMENT = "createPayment";
    private static final String METHOD_APPLY_WEBHOOK = "applyWebhook";
    private static final String METHOD_UPDATE_STATUS = "updateStatus";

    private final PaymentRepository paymentRepository;
    private final CustomerService customerService;
    private final ObjectMapper objectMapper;
    private final com.conectaai.adapter.factory.PaymentGatewayAdapterFactory adapterFactory;

    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDto paymentRequest) {
        LOGGER.info(METHOD_CREATE_PAYMENT, "Criando pagamento para o cliente ID: {}", paymentRequest.customerId());

        validatePaymentRequest(paymentRequest);

        Customer customer = customerService.findCustomerEntityById(paymentRequest.customerId());

        Payment payment = PaymentMapper.toEntity(paymentRequest, customer);

        validateExternalIdUniqueness(payment.getExternalId());

        try {
            com.conectaai.adapter.gateway.PaymentGatewayAdapter adapter = 
                    adapterFactory.getAdapter(paymentRequest.provider());
            
            com.conectaai.adapter.gateway.GatewayPaymentResponse gatewayResponse = adapter.createPayment(
                    customer,
                    payment.getAmount(),
                    payment.getCurrency().name(),
                    payment.getPaymentMethod(),
                    payment.getDescription(),
                    payment.getDueDate(),
                    payment.getExternalId()
            );

            payment.setProviderPaymentId(gatewayResponse.providerPaymentId());
            payment.setStatus(com.conectaai.utils.GatewayStatusMapper.mapPaymentStatus(gatewayResponse.status()));
            payment.setPaymentUrl(gatewayResponse.paymentUrl());
            payment.setQrCode(gatewayResponse.qrCode());
            payment.setBarCode(gatewayResponse.barCode());
            if (gatewayResponse.paidAt() != null) {
                payment.setPaidAt(gatewayResponse.paidAt().toLocalDateTime());
            }
        } catch (com.conectaai.exception.GatewayException e) {
            LOGGER.error(METHOD_CREATE_PAYMENT, "Erro ao criar pagamento no gateway", e);
            throw e;
        }

        Payment savedPayment = paymentRepository.save(payment);
        LOGGER.info(METHOD_CREATE_PAYMENT,
                "Pagamento criado com sucesso. ID: {}, ExternalID: {}, ProviderPaymentID: {}",
                savedPayment.getId(), savedPayment.getExternalId(), savedPayment.getProviderPaymentId());

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
        LOGGER.info(METHOD_UPDATE_STATUS, "Atualizando status do pagamento ID: {}", id);

        Payment payment = findByIdOrThrow(id);

        if (updateRequest.status() != null) {
            validateStatusTransition(payment.getStatus(), updateRequest.status());
        }

        PaymentMapper.updateEntity(payment, updateRequest);

        Payment updatedPayment = paymentRepository.save(payment);
        LOGGER.info(METHOD_UPDATE_STATUS,
                "Status do pagamento atualizado com sucesso. ID: {}, Novo Status: {}",
                updatedPayment.getId(), updatedPayment.getStatus());

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

        if (payment.getProviderPaymentId() != null && !payment.getProviderPaymentId().isBlank()) {
            try {
                com.conectaai.adapter.gateway.PaymentGatewayAdapter adapter = 
                        adapterFactory.getAdapter(payment.getProvider());
                adapter.cancelPayment(payment.getProviderPaymentId());
            } catch (com.conectaai.exception.GatewayException e) {
                LOGGER.error("cancelPayment", "Erro ao cancelar pagamento no gateway", e);
                throw e;
            }
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

    @Transactional
    public void applyWebhook(Provider provider, PaymentStatus status, String rawPayload) {
        LOGGER.info(METHOD_APPLY_WEBHOOK, "Processando webhook de pagamento: provider={}, status={}", provider, status);
        
        String externalId = extractExternalId(provider, rawPayload);
        if (externalId == null) {
            String providerPaymentId = extractProviderPaymentId(provider, rawPayload);
            if (providerPaymentId != null) {
                paymentRepository.findByProviderAndProviderPaymentId(provider, providerPaymentId)
                        .ifPresentOrElse(
                                payment -> updatePaymentStatus(payment, status),
                                () -> LOGGER.warn(METHOD_APPLY_WEBHOOK, 
                                        "Pagamento não encontrado: provider={} providerPaymentId={}", 
                                        provider, providerPaymentId)
                        );
            } else {
                LOGGER.warn(METHOD_APPLY_WEBHOOK, "external_id e provider_payment_id ausentes no payload");
            }
            return;
        }

        paymentRepository.findByExternalId(externalId).ifPresentOrElse(
                payment -> updatePaymentStatus(payment, status),
                () -> LOGGER.warn(METHOD_APPLY_WEBHOOK, "Pagamento não encontrado: externalId={}", externalId)
        );
    }

    private String extractExternalId(Provider provider, String rawPayload) {
        try {
            JsonNode payload = objectMapper.readTree(rawPayload);
            return switch (provider) {
                case STRIPE -> {
                    String id = extractString(payload.at("/data/object/metadata/external_id"));
                    if (id == null) {
                        id = extractString(payload.at("/data/object/external_id"));
                    }
                    yield id;
                }
                case MERCADO_PAGO -> extractString(payload.at("/data/external_reference"));
                case ASAAS -> {
                    String id = extractString(payload.at("/payment/externalReference"));
                    if (id == null) {
                        JsonNode externalRefNode = payload.get("externalReference");
                        id = extractString(externalRefNode);
                    }
                    yield id;
                }
            };
        } catch (Exception e) {
            LOGGER.warn("extractExternalId", "Erro ao extrair external_id: {}", e.getMessage());
            return null;
        }
    }

    private String extractProviderPaymentId(Provider provider, String rawPayload) {
        try {
            JsonNode payload = objectMapper.readTree(rawPayload);
            return switch (provider) {
                case STRIPE -> extractString(payload.at("/data/object/id"));
                case MERCADO_PAGO -> extractString(payload.at("/data/id"));
                case ASAAS -> extractString(payload.at("/payment/id"));
            };
        } catch (Exception e) {
            LOGGER.warn("extractProviderPaymentId", "Erro ao extrair provider_payment_id: {}", e.getMessage());
            return null;
        }
    }

    private String extractString(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        return node.asText();
    }

    private void updatePaymentStatus(com.conectaai.domain.Payment payment, PaymentStatus newStatus) {
        if (newStatus != null && PaymentUtils.isValidStatusTransition(payment.getStatus(), newStatus)) {
            payment.setStatus(newStatus);
            if (newStatus == PaymentStatus.CONFIRMED || newStatus == PaymentStatus.RECEIVED) {
                payment.setPaidAt(java.time.LocalDateTime.now());
            }
            paymentRepository.save(payment);
            LOGGER.info("updatePaymentStatus", 
                    "Pagamento {} atualizado para {}", payment.getId(), newStatus);
        } else {
            LOGGER.warn("updatePaymentStatus", 
                    "Transição inválida ignorada: {} -> {}", payment.getStatus(), newStatus);
        }
    }

}

