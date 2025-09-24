package com.conectaai.repository;

import com.conectaai.domain.Payment;
import com.conectaai.domain.Refund;
import com.conectaai.enums.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByPayment(Payment payment);

    List<Refund> findByStatus(RefundStatus refundStatus);

    List<Refund> findByPaymentAndStatus(Payment payment, RefundStatus status);

    List<Refund> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Refund> findByCreatedAtAfter(LocalDateTime date);

    Boolean existsByPayment(Payment payment);
    boolean existsByProviderReference(String providerReference);

    Optional<Refund> findByProviderReference(String providerReference);

    List<Refund> findByPaymentId(Long paymentId);

    List<Refund> findByAmount(BigDecimal amount);
    List<Refund> findByAmountGreaterThan(BigDecimal amount);
    List<Refund> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    long countByStatus(RefundStatus status);
    long countByPayment(Payment payment);

    Page<Refund> findByStatus(RefundStatus status, Pageable pageable);
    Page<Refund> findByPayment(Payment payment, Pageable pageable);

    @Query("SELECT r FROM Refund r WHERE r.payment.customer.id = :customerId")
    List<Refund> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT SUM(r.amount) FROM Refund r WHERE r.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") RefundStatus status);

    @Query("SELECT r FROM Refund r WHERE r.status = com.conectaai.enums.RefundStatus.PENDING AND r.createdAt < :date")
    List<Refund> findPendingRefundsOlderThan(@Param("date") LocalDateTime date);
}
