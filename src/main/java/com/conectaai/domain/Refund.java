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
@Table(name = "refund")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(precision = 19, scale = 2)
    @NotNull
    private BigDecimal amount;

    @NotNull @Enumerated(EnumType.STRING)
    private RefundStatus status;

    @Size(max = 500)
    @Column(length = 500)
    private String reason;

    @Size(max = 100)
    private String providerReference;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    private void normalizeData() {
        if (this.providerReference != null) {
            this.providerReference = this.providerReference.trim();
        }
        if (this.reason != null) {
            this.reason = this.reason.trim();
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
