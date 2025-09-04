package com.conectaai.repository;

import com.conectaai.domain.Payment;
import com.conectaai.domain.Refund;
import com.conectaai.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByPayment (Payment payment);

    List<Refund> findByStatus (RefundStatus refundStatus);

    List<Refund> findByPaymentAndStatus(Payment payment, RefundStatus status);

    List<Refund> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Boolean existsByPayment(Payment payment);

    Optional<Refund> findByProviderReference (String providerReference);
}
