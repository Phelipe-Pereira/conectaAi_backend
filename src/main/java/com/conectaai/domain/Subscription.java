package com.conectaai.domain;

import com.conectaai.enums.Currency;
import com.conectaai.enums.Provider;
import com.conectaai.enums.SubscriptionInterval;
import com.conectaai.enums.SubscriptionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "subscription",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_subscription_provider_subscription_id",
                columnNames = {"provider", "provider_subscription_id"}
        ), indexes = {
        @Index(name = "idx_subscription_external_id", columnList = "external_id"),
        @Index(name = "idx_subscription_status", columnList = "status"),
        @Index(name = "idx_subscription_customer_id", columnList = "customer_id"),
        @Index(name = "idx_subscription_provider", columnList = "provider"),
        @Index(name = "idx_subscription_start_at", columnList = "startAt")
})
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    @Column(name = "external_id", unique = true)
    @Size(max = 100)
    private String externalId;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(precision = 19, scale = 2)
    @NotNull
    private BigDecimal amount;

    @NotNull @Enumerated(EnumType.STRING)
    private Currency currency;

    @NotNull @Enumerated(EnumType.STRING)
    private SubscriptionInterval interval;

    @NotNull
    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @NotNull @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @NotNull
    @Size(max = 50)
    @Column(name = "payment_method", length = 50, nullable = false)
    private String paymentMethod;

    @NotNull @Enumerated(EnumType.STRING)
    private Provider provider;

    @Size(max = 100)
    @Column(name = "provider_subscription_id")
    private String providerSubscriptionId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    @PreUpdate
    private void normalizeData() {
        this.updatedAt = LocalDateTime.now();
        if (this.providerSubscriptionId != null) {
            this.providerSubscriptionId = this.providerSubscriptionId.trim();
        }
        if (this.description != null) {
            this.description = this.description.trim();
        }
        if (this.paymentMethod != null) {
            this.paymentMethod = this.paymentMethod.trim().toUpperCase();
        }
        if (this.amount != null && this.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da assinatura deve ser maior que zero");
        }
        if (this.endAt != null && this.endAt.isBefore(this.startAt)) {
            throw new IllegalArgumentException("Data de término deve ser posterior à data de início");
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
        Subscription subscription = (Subscription) o;
        return Objects.equals(id, subscription.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}


