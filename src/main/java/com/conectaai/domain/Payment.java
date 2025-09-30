package com.conectaai.domain;

import com.conectaai.enums.Currency;

import com.conectaai.enums.PaymentStatus;
import com.conectaai.enums.Provider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String publicId;

    @ManyToOne
    @NotNull
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

    private String providerReference;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
