package com.conectaai.domain;

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
@Table(name = "idempotency")
public class Idempotency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    @Column(unique = true)
    @Size(max = 255)
    private String idempotencyKey;

    @NotNull
    @Size(max = 255)
    private String requestHash;

    @Column(columnDefinition = "text")
    private String responseBody;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Idempotency idempotency = (Idempotency) o;
        return Objects.equals(id, idempotency.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
