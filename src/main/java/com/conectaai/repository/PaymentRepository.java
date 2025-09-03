package com.conectaai.repository;

import com.conectaai.domain.Payment;
import com.conectaai.enums.PaymentStatus;
import com.conectaai.enums.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPublicId(String publicId);
    List<Payment> findByCustomerId(Long customerId);
    Optional<Payment> findByProviderAndProviderReference(Provider provider, String providerReference);
    List<Payment> findByProvider(Provider provider);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByCustomerIdAndStatus(Long customerId, PaymentStatus status);
    List<Payment> findByProviderAndStatus(Provider provider, PaymentStatus status);
    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime date);
    boolean existsByPublicId(String publicId);
    boolean existsByCustomerId(Long customerId);
    boolean existsByCustomerIdAndStatusIn(Long customerId, List<PaymentStatus> statuses);
    long countByStatus(PaymentStatus status);
    long countByProvider(Provider provider);
    long countByCustomerId(Long customerId);
    List<Payment> findByCreatedAtAfter(LocalDateTime date);
    List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Payment> findByDueDateBefore(LocalDateTime date);
    Page<Payment> findByCustomerId(Long customerId, Pageable pageable);
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
}