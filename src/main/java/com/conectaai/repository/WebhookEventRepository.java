package com.conectaai.repository;

import com.conectaai.domain.WebhookEvent;
import com.conectaai.enums.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    List<WebhookEvent> findByEventType(String eventType);

    List<WebhookEvent> findByProvider(Provider provider);

    List<WebhookEvent> findByReceivedAtBefore(LocalDateTime date);
    List<WebhookEvent> findByReceivedAtAfter(LocalDateTime date);
    List<WebhookEvent> findByReceivedAtBetween(LocalDateTime start, LocalDateTime end);

    List<WebhookEvent> findByProcessed(Boolean processed);

    List<WebhookEvent> findByProviderAndProcessed(Provider provider, Boolean processed);
    List<WebhookEvent> findByEventTypeAndProcessed(String eventType, Boolean processed);
    List<WebhookEvent> findByProviderAndEventType(Provider provider, String eventType);

    boolean existsByProviderAndEventTypeAndPayload(Provider provider, String eventType, String payload);

    Long countByProcessed(Boolean processed);
    Long countByProvider(Provider provider);
    Long countByEventType(String eventType);

    Page<WebhookEvent> findByProcessed(Boolean processed, Pageable pageable);
    Page<WebhookEvent> findByProvider(Provider provider, Pageable pageable);

    List<WebhookEvent> findByProcessedOrderByReceivedAtAsc(Boolean processed);

    List<WebhookEvent> findByProcessedAndReceivedAtBefore(Boolean processed, LocalDateTime date);

    @Query("SELECT w FROM WebhookEvent w WHERE w.payload LIKE %:content%")
    List<WebhookEvent> findByPayloadContaining(@Param("content") String content);

    @Modifying
    @Query("UPDATE WebhookEvent w SET w.processed = true WHERE w.id IN :ids")
    void markAsProcessed(@Param("ids") List<Long> ids);

    @Modifying
    @Query("DELETE FROM WebhookEvent w WHERE w.processed = true AND w.receivedAt < :date")
    void deleteProcessedEventsOlderThan(@Param("date") LocalDateTime date);

    @Query("SELECT w FROM WebhookEvent w " +
            "WHERE w.provider = :provider AND w.eventType = :eventType " +
            "AND w.payload = :payload ORDER BY w.receivedAt DESC")
    List<WebhookEvent> findDuplicateEvents(
            @Param("provider") Provider provider,
            @Param("eventType") String eventType,
            @Param("payload") String payload);
}
