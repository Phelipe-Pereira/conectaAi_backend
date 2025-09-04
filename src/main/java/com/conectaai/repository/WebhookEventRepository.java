package com.conectaai.repository;

import com.conectaai.domain.WebhookEvent;
import com.conectaai.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    List<WebhookEvent> findByEventType (String eventType);
    List<WebhookEvent> findByProvider (Provider provider);
    List<WebhookEvent> findByReceivedAtBefore(LocalDateTime date);
    List<WebhookEvent> findByProcessed(Boolean processed);
    List<WebhookEvent> findByProviderAndProcessed(Provider provider, Boolean processed);
    List<WebhookEvent> findByEventTypeAndProcessed(String eventType, Boolean processed);
    List<WebhookEvent> findByReceivedAtBetween(LocalDateTime start, LocalDateTime end);
    Long countByProcessed(Boolean processed);
}
