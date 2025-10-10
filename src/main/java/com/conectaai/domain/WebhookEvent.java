package com.conectaai.domain;

import com.conectaai.enums.Provider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "webhook_event", indexes = {
        @Index(name = "idx_webhook_processed", columnList = "processed"),
        @Index(name = "idx_webhook_received_at", columnList = "receivedAt"),
        @Index(name = "idx_webhook_provider", columnList = "provider")
})
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    private Provider provider;

    @NotNull
    @Size(max = 100)
    private String eventType;

    @Column(columnDefinition = "jsonb")
    @NotNull
    @Size(max = 10000)
    private String payload;

    @Column(nullable = false)
    private Boolean processed = false;

    @CreationTimestamp
    @Column(name = "received_at", updatable = false)
    private LocalDateTime receivedAt;

    @PrePersist
    @PreUpdate
    private void normalizeData() {
        if (this.eventType != null) {
            this.eventType = this.eventType.toLowerCase().trim();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebhookEvent webhookEvent = (WebhookEvent) o;
        return Objects.equals(id, webhookEvent.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
