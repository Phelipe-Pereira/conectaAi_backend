package com.conectaai.domain;

import com.conectaai.enums.Currency;
import com.conectaai.enums.Provider;
import com.conectaai.enums.SubscriptionInterval;
import com.conectaai.enums.SubscriptionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription")
@Getter
@Setter
@NoArgsConstructor
public class Subscription {

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
    private SubscriptionInterval interval;

    @NotNull
    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @NotNull @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    @NotNull @Enumerated(EnumType.STRING)
    private Provider provider;

    private String providerReference;

    @CreationTimestamp
    @NotNull
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}


