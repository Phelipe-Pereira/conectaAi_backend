package com.conectaai.consumer;

import com.conectaai.domain.Idempotency;
import com.conectaai.enums.PaymentStatus;
import com.conectaai.logger.AppLogger;
import com.conectaai.messaging.WebhookMessage;
import com.conectaai.repository.IdempotencyRepository;
import com.conectaai.service.payment.PaymentService;
import com.conectaai.utils.IdempotencyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentWebhookConsumer {

    private static final AppLogger LOGGER = AppLogger.getLogger(PaymentWebhookConsumer.class);
    private static final String METHOD_ON_MESSAGE = "onMessage";

    private final IdempotencyRepository idempotencyRepository;
    private final PaymentService paymentService;

    @RabbitListener(queues = "webhook.payment.q")
    public void onMessage(WebhookMessage message) {
        try {
            LOGGER.info(METHOD_ON_MESSAGE, "Processando webhook de pagamento: eventId={}, eventType={}, provider={}",
                    message.eventId(), message.eventType(), message.provider());

            if (idempotencyRepository.existsByIdempotencyKey(message.eventId())) {
                LOGGER.info(METHOD_ON_MESSAGE,
                        "Webhook já processado anteriormente, ignorando: eventId={}", message.eventId());
                return;
            }

            PaymentStatus status = translateStatus(message.provider(), message.eventType());
            if (status != null) {
                paymentService.applyWebhook(message.provider(), status, message.payload());
            } else {
                LOGGER.warn(METHOD_ON_MESSAGE, 
                        "Status não mapeado para evento: provider={} eventType={}", 
                        message.provider(), message.eventType());
            }

            Idempotency idem = new Idempotency();
            idem.setIdempotencyKey(message.eventId());
            idem.setRequestHash(IdempotencyUtils.calculateRequestHash(message.payload()));
            idempotencyRepository.save(idem);

            LOGGER.info(METHOD_ON_MESSAGE, "Webhook processado com sucesso: eventId={}", message.eventId());
        } catch (Exception e) {
            LOGGER.error(METHOD_ON_MESSAGE,
                    "Erro ao processar webhook de pagamento: eventId={}", message.eventId(), e);
            throw e;
        }
    }

    private PaymentStatus translateStatus(com.conectaai.enums.Provider provider, String eventType) {
        String e = eventType == null ? "" : eventType.toLowerCase();
        return switch (provider) {
            case STRIPE -> translateStripeStatus(e);
            case MERCADO_PAGO -> translateMercadoPagoStatus(e);
            case ASAAS -> translateAsaasStatus(e);
        };
    }

    private PaymentStatus translateStripeStatus(String eventType) {
        if (eventType.contains("payment_intent.succeeded") || eventType.contains("charge.succeeded")) {
            return PaymentStatus.CONFIRMED;
        }
        if (eventType.contains("payment_intent.payment_failed") || eventType.contains("charge.failed")) {
            return PaymentStatus.FAILED;
        }
        if (eventType.contains("refund") || eventType.contains("charge.refunded")) {
            return PaymentStatus.REFUNDED;
        }
        if (eventType.contains("cancel")) {
            return PaymentStatus.CANCELLED;
        }
        return null;
    }

    private PaymentStatus translateMercadoPagoStatus(String eventType) {
        if (eventType.contains("approved") || eventType.contains("authorized")) {
            return PaymentStatus.CONFIRMED;
        }
        if (eventType.contains("rejected") || eventType.contains("cancelled")) {
            return PaymentStatus.CANCELLED;
        }
        if (eventType.contains("refunded") || eventType.contains("charged_back")) {
            return PaymentStatus.REFUNDED;
        }
        if (eventType.contains("pending")) {
            return PaymentStatus.PENDING;
        }
        return null;
    }

    private PaymentStatus translateAsaasStatus(String eventType) {
        String e = eventType.toUpperCase();
        if (e.contains("RECEIVED") || e.contains("CONFIRMED")) {
            return PaymentStatus.CONFIRMED;
        }
        if (e.contains("DELETED") || e.contains("CANCELLED")) {
            return PaymentStatus.CANCELLED;
        }
        if (e.contains("REFUNDED")) {
            return PaymentStatus.REFUNDED;
        }
        if (e.contains("PENDING") || e.contains("AWAITING")) {
            return PaymentStatus.PENDING;
        }
        if (e.contains("OVERDUE")) {
            return PaymentStatus.EXPIRED;
        }
        return null;
    }
}
