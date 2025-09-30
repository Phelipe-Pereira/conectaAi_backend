package com.conectaai.domain;

import com.conectaai.enums.DocumentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private String email;

    @Column(unique = true)
    private String document;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentType documentType;

    private String phone;

    private String address;

    private Integer addressNumber;

    private String complement;

    private String city;

    private String state;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(length = 2)
    private String country = "BR";

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "company_name")
    private String companyName;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}