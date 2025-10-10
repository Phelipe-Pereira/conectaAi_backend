package com.conectaai.domain;

import com.conectaai.enums.Currency;

import com.conectaai.enums.PaymentStatus;
import com.conectaai.enums.Provider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "payment",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payment_provider_ref",
                columnNames = {"provider", "provider_reference"}
        ), indexes = {
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_customer_id", columnList = "customer_id"),
        @Index(name = "idx_payment_provider", columnList = "provider"),
        @Index(name = "idx_payment_created_at", columnList = "createdAt")
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    @Column(unique = true)
    @Size(max = 100)
    private String publicId;

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
    private PaymentStatus status;

    private LocalDateTime dueDate;

    @NotNull @Enumerated(EnumType.STRING)
    private Provider provider;

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
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
