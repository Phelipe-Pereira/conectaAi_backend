package com.conectaai.domain;

import com.conectaai.enums.Currency;
import com.conectaai.enums.PaymentStatus;
import com.conectaai.enums.Provider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "payment",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payment_provider_payment_id",
                columnNames = {"provider", "provider_payment_id"}
        ),
        indexes = {
                @Index(name = "idx_payment_external_id", columnList = "external_id"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_customer_id", columnList = "customer_id"),
                @Index(name = "idx_payment_provider", columnList = "provider"),
                @Index(name = "idx_payment_created_at", columnList = "created_at"),
                @Index(name = "idx_payment_due_date", columnList = "due_date")
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    @Column(name = "external_id", unique = true)
    @Size(max = 100)
    private String externalId;

    @Size(max = 100)
    @Column(name = "provider_payment_id")
    private String providerPaymentId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(precision = 19, scale = 2, nullable = false)
    @NotNull
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @NotNull
    @Size(max = 50)
    @Column(name = "payment_method", length = 50, nullable = false)
    private String paymentMethod;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Size(max = 500)
    @Column(name = "payment_url", length = 500)
    private String paymentUrl;

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    @Size(max = 100)
    @Column(name = "bar_code", length = 100)
    private String barCode;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    @PreUpdate
    private void normalizeData() {
        if (this.providerPaymentId != null) {
            this.providerPaymentId = this.providerPaymentId.trim();
        }
        if (this.description != null) {
            this.description = this.description.trim();
        }
        if (this.paymentMethod != null) {
            this.paymentMethod = this.paymentMethod.trim().toUpperCase();
        }
        if (this.amount != null && this.amount.compareTo(new BigDecimal("5.00")) < 0) {
            throw new IllegalStateException("Valor mínimo do pagamento é R$ 5,00");
        }
        if (this.dueDate == null) {
            throw new IllegalStateException("Data de vencimento é obrigatória");
        }
        if (this.dueDate.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Data de vencimento deve ser no futuro");
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
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
