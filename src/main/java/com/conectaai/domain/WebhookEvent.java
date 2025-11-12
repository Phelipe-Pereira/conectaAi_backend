package com.conectaai.domain;

import com.conectaai.enums.Provider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "webhook_event", indexes = {
        @Index(name = "idx_webhook_processed", columnList = "processed"),
        @Index(name = "idx_webhook_received_at", columnList = "receivedAt"),
        @Index(name = "idx_webhook_provider", columnList = "provider"),
        @Index(name = "idx_webhook_endpoint", columnList = "webhook_endpoint_id")
})
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_endpoint_id", nullable = true)
    private WebhookEndpoint webhookEndpoint;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @NotNull
    @Size(max = 100)
    private String eventType;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @NotNull
    @Size(max = 10000)
    private String payload;

    @Column(nullable = false)
    @Builder.Default
    private Boolean processed = false;

    @CreationTimestamp
    @Column(name = "received_at", updatable = false)
    private LocalDateTime receivedAt;

    @Size(max = 100)
    @Column(name = "external_id", length = 100, unique = true)
    private String externalId;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Size(max = 1000)
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Size(max = 500)
    @Column(length = 500)
    private String signature;

    @PrePersist
    @PreUpdate
    private void normalizeData() {
        if (this.eventType != null) {
            this.eventType = this.eventType.toLowerCase().trim();
        }
        if (this.externalId != null) {
            this.externalId = this.externalId.trim();
        }
        if (this.errorMessage != null) {
            this.errorMessage = this.errorMessage.trim();
        }
        if (this.signature != null) {
            this.signature = this.signature.trim();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WebhookEvent webhookEvent = (WebhookEvent) o;
        return Objects.equals(id, webhookEvent.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
