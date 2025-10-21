package com.conectaai.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "webhook_endpoint", indexes = {
        @Index(name = "idx_webhook_endpoint_active", columnList = "active"),
        @Index(name = "idx_webhook_endpoint_user", columnList = "user_id"),
        @Index(name = "idx_webhook_endpoint_created_at", columnList = "createdAt")
})
public class WebhookEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @Column(nullable = false)
    @NotNull
    @Size(max = 500)
    private String url;

    @Column(nullable = false)
    @NotNull
    @Size(max = 100)
    private String secret;

    @Column(columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    @NotNull
    @Builder.Default
    private List<String> enabledEvents = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "webhookEndpoint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WebhookEvent> events = new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void normalizeData() {
        if (this.url != null) {
            this.url = this.url.trim().toLowerCase();
        }
        if (this.secret != null) {
            this.secret = this.secret.trim();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebhookEndpoint that = (WebhookEndpoint) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

