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

    @Query(value = "SELECT * FROM webhook_event WHERE payload->>:fieldName = :value", nativeQuery = true)
    List<WebhookEvent> findByFieldInPayload(@Param("fieldName") String fieldName, @Param("value") String value);

    @Query(value = "SELECT * FROM webhook_event WHERE payload->>'customer_id' = :customerId", nativeQuery = true)
    List<WebhookEvent> findByCustomerIdInPayload(@Param("customerId") String customerId);

    @Query(value = "SELECT * FROM webhook_event WHERE payload->>'payment_id' = :paymentId", nativeQuery = true)
    List<WebhookEvent> findByPaymentIdInPayload(@Param("paymentId") String paymentId);

    @Query(value = "SELECT * FROM webhook_event WHERE payload->>'external_reference' = :externalRef",
            nativeQuery = true)
    List<WebhookEvent> findByExternalReferenceInPayload(@Param("externalRef") String externalRef);

    @Modifying
    @Query("UPDATE WebhookEvent w SET w.processed = true WHERE w.id IN :ids")
    void markAsProcessed(@Param("ids") List<Long> ids);

    @Modifying
    @Query("DELETE FROM WebhookEvent w WHERE w.processed = true AND w.receivedAt < :date")
    void deleteProcessedEventsOlderThan(@Param("date") LocalDateTime date);

    @Query("SELECT w FROM WebhookEvent w WHERE w.provider = :provider "
            + "AND w.eventType = :eventType AND w.payload = :payload ORDER BY w.receivedAt DESC")
    List<WebhookEvent> findDuplicateEvents(
            @Param("provider") Provider provider,
            @Param("eventType") String eventType,
            @Param("payload") String payload);
}
