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
    private static final String METHOD_CREATE_SUBSCRIPTION = "createSubscription";
    private static final String METHOD_APPLY_WEBHOOK = "applyWebhook";
    private static final String METHOD_UPDATE_STATUS = "updateStatus";

    private final SubscriptionRepository subscriptionRepository;
    private final CustomerService customerService;
    private final com.conectaai.adapter.factory.SubscriptionGatewayAdapterFactory adapterFactory;

    @Transactional
    public SubscriptionResponseDto createSubscription(SubscriptionRequestDto subscriptionRequest) {
        LOGGER.info(METHOD_CREATE_SUBSCRIPTION,
                "Iniciando criação de assinatura para customer {}", subscriptionRequest.customerId());

        Customer customer = customerService.findCustomerEntityById(subscriptionRequest.customerId());

        if (Boolean.FALSE.equals(customer.getActive())) {
            throw new IllegalArgumentException("Cliente inativo não pode criar assinaturas");
        }

        validateSubscriptionRequest(subscriptionRequest);

        Subscription subscription = SubscriptionMapper.toEntity(subscriptionRequest, customer);

        try {
            com.conectaai.adapter.gateway.SubscriptionGatewayAdapter adapter = 
                    adapterFactory.getAdapter(subscriptionRequest.provider());
            
            com.conectaai.adapter.gateway.GatewaySubscriptionResponse gatewayResponse = adapter.createSubscription(
                    customer,
                    subscription.getAmount(),
                    subscription.getCurrency().name(),
                    subscription.getInterval().name(),
                    subscription.getPaymentMethod(),
                    subscription.getDescription(),
                    subscription.getStartAt(),
                    subscription.getEndAt(),
                    subscription.getExternalId()
            );

            subscription.setProviderSubscriptionId(gatewayResponse.providerSubscriptionId());
            String gatewayStatus = gatewayResponse.status();
            subscription.setStatus(com.conectaai.utils.GatewayStatusMapper.mapSubscriptionStatus(gatewayStatus));
        } catch (com.conectaai.exception.GatewayException e) {
            LOGGER.error(METHOD_CREATE_SUBSCRIPTION, "Erro ao criar assinatura no gateway", e);
            throw e;
        }

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        LOGGER.info(METHOD_CREATE_SUBSCRIPTION, 
                "Assinatura criada com sucesso: {}, ProviderSubscriptionID: {}", 
                savedSubscription.getExternalId(), savedSubscription.getProviderSubscriptionId());
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
        LOGGER.info(METHOD_UPDATE_STATUS, "Atualizando status da assinatura: {}", id);

        Subscription subscription = findByIdOrThrow(id);

        if (updateRequest.status() != null) {
            validateStatusTransition(subscription.getStatus(), updateRequest.status());
            LOGGER.info(METHOD_UPDATE_STATUS,
                    "Transição de status: {} -> {}", subscription.getStatus(), updateRequest.status());
        }

        SubscriptionMapper.updateEntity(subscription, updateRequest);
        Subscription updatedSubscription = subscriptionRepository.save(subscription);

        LOGGER.info(METHOD_UPDATE_STATUS, "Assinatura atualizada com sucesso");
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

        if (!SubscriptionUtils.isPaymentMethodSupported(
                subscriptionRequest.provider(), subscriptionRequest.paymentMethod())) {
            throw new UnsupportedPaymentMethodException(
                    String.format("Método de pagamento %s não é suportado pelo provedor %s",
                            subscriptionRequest.paymentMethod(), subscriptionRequest.provider())
            );
        }

        if (subscriptionRequest.endAt() != null
                && subscriptionRequest.endAt().isBefore(subscriptionRequest.startAt())) {
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

    @Transactional
    public void applyWebhook(Provider provider, String eventType, String rawPayload) {
        LOGGER.info(METHOD_APPLY_WEBHOOK, "Webhook de assinatura recebido: provider={}, event={}", provider, eventType);
        
        SubscriptionStatus status = translateStatus(provider, eventType);
        if (status == null) {
            LOGGER.warn(METHOD_APPLY_WEBHOOK, "Status não mapeado para evento: provider={} event={}", provider, eventType);
            return;
        }

        String externalId = extractExternalId(provider, rawPayload);
        if (externalId != null) {
            updateSubscriptionByExternalId(externalId, status);
            return;
        }

        updateSubscriptionByProviderId(provider, rawPayload, status);
    }

    private void updateSubscriptionByExternalId(String externalId, SubscriptionStatus status) {
        subscriptionRepository.findByExternalId(externalId).ifPresentOrElse(
                subscription -> updateSubscriptionStatus(subscription, status),
                () -> LOGGER.warn(METHOD_APPLY_WEBHOOK, "Assinatura não encontrada: externalId={}", externalId)
        );
    }

    private void updateSubscriptionByProviderId(Provider provider, String rawPayload, SubscriptionStatus status) {
        String providerSubscriptionId = extractProviderSubscriptionId(provider, rawPayload);
        if (providerSubscriptionId == null) {
            LOGGER.warn(METHOD_APPLY_WEBHOOK, "external_id e provider_subscription_id ausentes no payload");
            return;
        }

        subscriptionRepository.findByProviderAndProviderSubscriptionId(provider, providerSubscriptionId)
                .ifPresentOrElse(
                        subscription -> updateSubscriptionStatus(subscription, status),
                        () -> LOGGER.warn(METHOD_APPLY_WEBHOOK, 
                                "Assinatura não encontrada: provider={} providerSubscriptionId={}", 
                                provider, providerSubscriptionId)
                );
    }

    private SubscriptionStatus translateStatus(Provider provider, String eventType) {
        String e = eventType != null ? eventType.toLowerCase() : "";
        return switch (provider) {
            case STRIPE -> translateStripeStatus(e);
            case MERCADO_PAGO -> translateMercadoPagoStatus(e);
            case ASAAS -> translateAsaasStatus(e);
        };
    }

    private SubscriptionStatus translateStripeStatus(String eventType) {
        if (eventType.contains("created") || eventType.contains("trialing")) {
            return SubscriptionStatus.TRIALING;
        }
        if (eventType.contains("active") || eventType.contains("succeeded")) {
            return SubscriptionStatus.ACTIVE;
        }
        if (eventType.contains("past_due") || eventType.contains("unpaid")) {
            return SubscriptionStatus.PAST_DUE;
        }
        if (eventType.contains("paused")) {
            return SubscriptionStatus.PAUSED;
        }
        if (eventType.contains("cancel") || eventType.contains("deleted")) {
            return SubscriptionStatus.CANCELLED;
        }
        if (eventType.contains("expired") || eventType.contains("incomplete_expired")) {
            return SubscriptionStatus.EXPIRED;
        }
        if (eventType.contains("incomplete")) {
            return SubscriptionStatus.INCOMPLETE;
        }
        return null;
    }

    private SubscriptionStatus translateMercadoPagoStatus(String eventType) {
        if (eventType.contains("authorized") || eventType.contains("approved")) {
            return SubscriptionStatus.ACTIVE;
        }
        if (eventType.contains("pending")) {
            return SubscriptionStatus.PENDING;
        }
        if (eventType.contains("cancelled") || eventType.contains("canceled")) {
            return SubscriptionStatus.CANCELLED;
        }
        return null;
    }

    private SubscriptionStatus translateAsaasStatus(String eventType) {
        String e = eventType.toUpperCase();
        if (e.contains("CREATED") || e.contains("PENDING")) {
            return SubscriptionStatus.PENDING;
        }
        if (e.contains("ACTIVATED") || e.contains("ACTIVE")) {
            return SubscriptionStatus.ACTIVE;
        }
        if (e.contains("INACTIVATED") || e.contains("PAST_DUE")) {
            return SubscriptionStatus.PAST_DUE;
        }
        if (e.contains("CANCELLED") || e.contains("DELETED")) {
            return SubscriptionStatus.CANCELLED;
        }
        return null;
    }

    private String extractExternalId(Provider provider, String rawPayload) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode payload = mapper.readTree(rawPayload);
            
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
                    String id = extractString(payload.at("/subscription/externalReference"));
                    if (id == null) {
                        id = extractString(payload.at("/payment/externalReference"));
                    }
                    yield id;
                }
            };
        } catch (Exception e) {
            LOGGER.warn("extractExternalId", "Erro ao extrair external_id: {}", e.getMessage());
            return null;
        }
    }

    private String extractProviderSubscriptionId(Provider provider, String rawPayload) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode payload = mapper.readTree(rawPayload);
            
            return switch (provider) {
                case STRIPE -> extractString(payload.at("/data/object/id"));
                case MERCADO_PAGO -> extractString(payload.at("/data/id"));
                case ASAAS -> extractString(payload.at("/subscription/id"));
            };
        } catch (Exception e) {
            LOGGER.warn("extractProviderSubscriptionId", 
                    "Erro ao extrair provider_subscription_id: {}", e.getMessage());
            return null;
        }
    }

    private String extractString(com.fasterxml.jackson.databind.JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        return node.asText();
    }

    private void updateSubscriptionStatus(Subscription subscription, SubscriptionStatus newStatus) {
        if (SubscriptionUtils.isValidStatusTransition(subscription.getStatus(), newStatus)) {
            subscription.setStatus(newStatus);
            subscriptionRepository.save(subscription);
            LOGGER.info("updateSubscriptionStatus", 
                    "Assinatura {} atualizada para {}", subscription.getId(), newStatus);
        } else {
            LOGGER.warn("updateSubscriptionStatus", 
                    "Transição inválida ignorada: {} -> {}", subscription.getStatus(), newStatus);
        }
    }
}

