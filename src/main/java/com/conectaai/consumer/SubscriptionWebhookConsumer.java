package com.conectaai.consumer;

import com.conectaai.domain.Idempotency;
import com.conectaai.logger.AppLogger;
import com.conectaai.messaging.WebhookMessage;
import com.conectaai.repository.IdempotencyRepository;
import com.conectaai.service.subscription.SubscriptionService;
import com.conectaai.utils.IdempotencyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionWebhookConsumer {

    private static final AppLogger LOGGER = AppLogger.getLogger(SubscriptionWebhookConsumer.class);
    private static final String METHOD_ON_MESSAGE = "onMessage";

    private final IdempotencyRepository idempotencyRepository;
    private final SubscriptionService subscriptionService;

    @RabbitListener(queues = "webhook.subscription.q")
    public void onMessage(WebhookMessage message) {
        try {
            LOGGER.info(METHOD_ON_MESSAGE, 
                    "Processando webhook de assinatura: eventId={}, eventType={}, provider={}",
                    message.eventId(), message.eventType(), message.provider());

            if (idempotencyRepository.existsByIdempotencyKey(message.eventId())) {
                LOGGER.info(METHOD_ON_MESSAGE,
                        "Webhook já processado anteriormente, ignorando: eventId={}", message.eventId());
                return;
            }

            subscriptionService.applyWebhook(message.provider(), message.eventType(), message.payload());

            Idempotency idem = new Idempotency();
            idem.setIdempotencyKey(message.eventId());
            idem.setRequestHash(IdempotencyUtils.calculateRequestHash(message.payload()));
            idempotencyRepository.save(idem);

            LOGGER.info(METHOD_ON_MESSAGE, "Webhook processado com sucesso: eventId={}", message.eventId());
        } catch (Exception e) {
            LOGGER.error(METHOD_ON_MESSAGE,
                    "Erro ao processar webhook de assinatura: eventId={}", message.eventId(), e);
            throw e;
        }
    }
}

