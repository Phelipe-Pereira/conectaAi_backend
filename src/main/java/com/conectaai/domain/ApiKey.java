package com.conectaai.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "api_keys", indexes = {
        @Index(name = "idx_api_key_key", columnList = "key_hash"),
        @Index(name = "idx_api_key_user", columnList = "user_id"),
        @Index(name = "idx_api_key_active", columnList = "active"),
        @Index(name = "idx_api_key_expires", columnList = "expires_at")
})
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "key_hash", nullable = false, length = 255, unique = true)
    private String keyHash;

    @Column(name = "key_prefix", nullable = false, length = 20)
    private String keyPrefix;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    public boolean isValid() {
        return Boolean.TRUE.equals(active) && !isExpired() && revokedAt == null;
    }

    public void revoke() {
        this.active = false;
        this.revokedAt = Instant.now();
    }

    public void markAsUsed() {
        this.lastUsedAt = Instant.now();
    }
}

