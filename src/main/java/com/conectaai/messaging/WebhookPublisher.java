package com.conectaai.messaging;

import com.conectaai.config.RabbitConfig;
import com.conectaai.logger.AppLogger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebhookPublisher {

    private static final AppLogger LOGGER = AppLogger.getLogger(WebhookPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public WebhookPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(WebhookMessage message, String targetType) {
        String normalizedEventType = normalizeEventType(message.eventType());
        String providerName = message.provider().name().toLowerCase();
        String routingKey = String.format("%s.%s.%s", targetType, providerName, normalizedEventType);
        
        String queueName = getQueueName(targetType);
        
        LOGGER.info("publish", "Publicando webhook: routingKey={} queue={} eventId={}", 
                routingKey, queueName, message.eventId());
        
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, message);
    }

    private String normalizeEventType(String eventType) {
        if (eventType == null) {
            return "unknown";
        }
        return eventType.toLowerCase()
                .replace(".", "_")
                .replace("-", "_")
                .replace(" ", "_");
    }

    private String getQueueName(String targetType) {
        return switch (targetType) {
            case "subscription" -> RabbitConfig.Q_SUBSCRIPTION;
            case "installment" -> RabbitConfig.Q_INSTALLMENT;
            default -> RabbitConfig.Q_PAYMENT;
        };
    }
}


