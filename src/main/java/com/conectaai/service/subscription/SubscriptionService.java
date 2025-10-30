package com.conectaai.service.subscription;

import com.conectaai.domain.Customer;
import com.conectaai.domain.Subscription;
import com.conectaai.dto.subscription.*;
import com.conectaai.enums.Provider;
import com.conectaai.enums.SubscriptionInterval;
import com.conectaai.enums.SubscriptionStatus;
import com.conectaai.exception.*;
import com.conectaai.logger.AppLogger;
import com.conectaai.repository.SubscriptionRepository;
import com.conectaai.service.customer.CustomerService;
import com.conectaai.specification.SubscriptionSpecification;
import com.conectaai.utils.SubscriptionUtils;
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
public class SubscriptionService {

    private static final AppLogger LOGGER = AppLogger.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final CustomerService customerService;

    @Transactional
    public SubscriptionResponseDto createSubscription(SubscriptionRequestDto subscriptionRequest) {
        LOGGER.info("createSubscription", "Iniciando criação de assinatura para customer {}", subscriptionRequest.customerId());

        Customer customer = customerService.findCustomerEntityById(subscriptionRequest.customerId());

        if (!customer.getActive()) {
            throw new IllegalArgumentException("Cliente inativo não pode criar assinaturas");
        }

        validateSubscriptionRequest(subscriptionRequest);

        Subscription subscription = SubscriptionMapper.toEntity(subscriptionRequest, customer);
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        LOGGER.info("createSubscription", "Assinatura criada com sucesso: {}", savedSubscription.getExternalId());
        return SubscriptionMapper.toResponseDto(savedSubscription);
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionSummaryDto> findSubscriptions(
            String externalId,
            Long customerId,
            Provider provider,
            SubscriptionStatus status,
            SubscriptionInterval interval,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDateTime startFrom,
            LocalDateTime startTo,
            LocalDateTime endFrom,
            LocalDateTime endTo,
            LocalDateTime createdStart,
            LocalDateTime createdEnd,
            Pageable pageable
    ) {
        LOGGER.info("findSubscriptions", "Buscando assinaturas com filtros aplicados");

        Specification<Subscription> spec = SubscriptionSpecification.hasExternalId(externalId)
                .and(SubscriptionSpecification.hasCustomerId(customerId))
                .and(SubscriptionSpecification.hasProvider(provider))
                .and(SubscriptionSpecification.hasStatus(status))
                .and(SubscriptionSpecification.hasInterval(interval))
                .and(SubscriptionSpecification.amountBetween(minAmount, maxAmount))
                .and(SubscriptionSpecification.startBetween(startFrom, startTo))
                .and(SubscriptionSpecification.endBetween(endFrom, endTo))
                .and(SubscriptionSpecification.createdBetween(createdStart, createdEnd));

        return subscriptionRepository.findAll(spec, pageable)
                .map(SubscriptionMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponseDto findById(Long id) {
        LOGGER.info("findById", "Buscando assinatura por ID: {}", id);
        Subscription subscription = findByIdOrThrow(id);
        return SubscriptionMapper.toResponseDto(subscription);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponseDto findByExternalId(String externalId) {
        LOGGER.info("findByExternalId", "Buscando assinatura por External ID: {}", externalId);
        Subscription subscription = subscriptionRepository.findByExternalId(externalId)
                .orElseThrow(() -> new SubscriptionNotFoundException("Assinatura não encontrada: " + externalId));
        return SubscriptionMapper.toResponseDto(subscription);
    }

    @Transactional
    public SubscriptionResponseDto updateStatus(Long id, SubscriptionUpdateDto updateRequest) {
        LOGGER.info("updateStatus", "Atualizando status da assinatura: {}", id);

        Subscription subscription = findByIdOrThrow(id);

        if (updateRequest.status() != null) {
            validateStatusTransition(subscription.getStatus(), updateRequest.status());
            LOGGER.info("updateStatus", "Transição de status: {} -> {}", subscription.getStatus(), updateRequest.status());
        }

        SubscriptionMapper.updateEntity(subscription, updateRequest);
        Subscription updatedSubscription = subscriptionRepository.save(subscription);

        LOGGER.info("updateStatus", "Assinatura atualizada com sucesso");
        return SubscriptionMapper.toResponseDto(updatedSubscription);
    }

    @Transactional
    public void cancelSubscription(Long id) {
        LOGGER.info("cancelSubscription", "Cancelando assinatura: {}", id);

        Subscription subscription = findByIdOrThrow(id);

        if (!SubscriptionUtils.isCancellable(subscription.getStatus())) {
            throw new IllegalArgumentException(
                    String.format("Assinatura com status %s não pode ser cancelada", subscription.getStatus())
            );
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);

        LOGGER.info("cancelSubscription", "Assinatura cancelada com sucesso");
    }

    @Transactional
    public void pauseSubscription(Long id) {
        LOGGER.info("pauseSubscription", "Pausando assinatura: {}", id);

        Subscription subscription = findByIdOrThrow(id);

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalArgumentException("Apenas assinaturas ativas podem ser pausadas");
        }

        subscription.setStatus(SubscriptionStatus.PAUSED);
        subscriptionRepository.save(subscription);

        LOGGER.info("pauseSubscription", "Assinatura pausada com sucesso");
    }

    @Transactional
    public SubscriptionResponseDto resumeSubscription(Long id) {
        LOGGER.info("resumeSubscription", "Retomando assinatura: {}", id);

        Subscription subscription = findByIdOrThrow(id);

        if (subscription.getStatus() != SubscriptionStatus.PAUSED) {
            throw new IllegalArgumentException("Apenas assinaturas pausadas podem ser retomadas");
        }

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        Subscription resumedSubscription = subscriptionRepository.save(subscription);

        LOGGER.info("resumeSubscription", "Assinatura retomada com sucesso");
        return SubscriptionMapper.toResponseDto(resumedSubscription);
    }

    private Subscription findByIdOrThrow(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException("Assinatura não encontrada: " + id));
    }

    private void validateSubscriptionRequest(SubscriptionRequestDto subscriptionRequest) {
        SubscriptionUtils.validateAmount(subscriptionRequest.amount());

        if (!SubscriptionUtils.isPaymentMethodSupported(subscriptionRequest.provider(), subscriptionRequest.paymentMethod())) {
            throw new UnsupportedPaymentMethodException(
                    String.format("Método de pagamento %s não é suportado pelo provedor %s",
                            subscriptionRequest.paymentMethod(), subscriptionRequest.provider())
            );
        }

        if (subscriptionRequest.endAt() != null && subscriptionRequest.endAt().isBefore(subscriptionRequest.startAt())) {
            throw new IllegalArgumentException("Data de término deve ser posterior à data de início");
        }
    }

    private void validateStatusTransition(SubscriptionStatus currentStatus, SubscriptionStatus newStatus) {
        if (!SubscriptionUtils.isValidStatusTransition(currentStatus, newStatus)) {
            throw new InvalidSubscriptionStatusTransitionException(
                    String.format("Transição de status inválida: %s -> %s", currentStatus, newStatus)
            );
        }
    }
}

