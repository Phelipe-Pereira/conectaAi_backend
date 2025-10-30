package com.conectaai.domain;

import com.conectaai.enums.RefundStatus;
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
        name = "refund",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_refund_provider_refund_id",
                columnNames = {"payment_id", "provider_refund_id"}
        ),
        indexes = {
                @Index(name = "idx_refund_external_id", columnList = "external_id"),
                @Index(name = "idx_refund_status", columnList = "status"),
                @Index(name = "idx_refund_payment_id", columnList = "payment_id"),
                @Index(name = "idx_refund_created_at", columnList = "created_at")
        }
)
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    @Column(name = "external_id", unique = true, nullable = false)
    @Size(max = 100)
    private String externalId;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(precision = 19, scale = 2, nullable = false)
    @NotNull
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    @Size(max = 500)
    @Column(length = 500)
    private String reason;

    @Size(max = 100)
    @Column(name = "provider_refund_id")
    private String providerRefundId;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

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
        
        if (this.providerRefundId != null) {
            this.providerRefundId = this.providerRefundId.trim();
        }
        if (this.reason != null) {
            this.reason = this.reason.trim();
        }
        
        if (this.amount != null && this.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do reembolso deve ser maior que zero");
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
        Refund refund = (Refund) o;
        return Objects.equals(id, refund.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
