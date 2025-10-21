package com.conectaai.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.conectaai.utils.CustomerUtils.isValidCNPJ;
import static com.conectaai.utils.CustomerUtils.isValidCPF;
import static com.conectaai.utils.DataNormalizer.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "customer", indexes = {
        @Index(name = "idx_customer_email", columnList = "email"),
        @Index(name = "idx_customer_active", columnList = "active"),
        @Index(name = "idx_customer_created_at", columnList = "createdAt")
})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    @Column(name = "external_id", unique = true)
    private String externalId;

    @NotNull
    @Size(min = 2, max = 100)
    private String firstName;

    @NotNull
    @Size(min = 2, max = 100)
    private String lastName;

    @Email
    @Column(unique = true)
    @NotNull
    @Size(max = 254)
    private String email;

    @Column(unique = true, length = 11)
    private String cpf;

    @Column(unique = true, length = 14)
    private String cnpj;

    @Size(max = 15)
    private String phone;

    @Size(max = 255)
    private String address;

    private Integer addressNumber;

    private String complement;

    @Size(max = 100)
    private String city;

    @Size(max = 2)
    private String state;

    @Column(name = "zip_code")
    @Size(max = 8)
    private String zipCode;

    @Column(length = 2)
    @Size(max = 2)
    private String country = "BR";

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "company_name")
    @Size(max = 100)
    private String companyName;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
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
        this.email = email(this.email);
        this.cpf = documentNumeric(this.cpf);
        this.cnpj = documentNumeric(this.cnpj);
        this.state = state(this.state);
        this.zipCode = zip(this.zipCode);
        this.firstName = name(this.firstName);
        this.lastName = name(this.lastName);
        this.phone = phone(this.phone);
        this.country = country(this.country);
        this.companyName = name(this.companyName);
        if (!hasAtLeastOneValidDocument()) throw new IllegalStateException("CPF ou CNPJ inválido ou ausente");
    }

    private boolean hasAtLeastOneValidDocument() {
        boolean okCpf = this.cpf != null && isValidCPF(this.cpf);
        boolean okCnpj = this.cnpj != null && isValidCNPJ(this.cnpj);
        return okCpf || okCnpj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
