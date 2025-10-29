package com.conectaai.repository;

import com.conectaai.domain.Payment;
import com.conectaai.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    Optional<Payment> findByExternalId(String externalId);

    Optional<Payment> findByProviderAndProviderPaymentId(Provider provider, String providerPaymentId);

    boolean existsByExternalId(String externalId);
}