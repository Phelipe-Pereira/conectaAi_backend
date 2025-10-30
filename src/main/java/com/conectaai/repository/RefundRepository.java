package com.conectaai.repository;

import com.conectaai.domain.Refund;
import com.conectaai.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long>, JpaSpecificationExecutor<Refund> {

    Optional<Refund> findByExternalId(String externalId);

    boolean existsByExternalId(String externalId);

    @Query("SELECT SUM(r.amount) FROM Refund r WHERE r.payment.id = :paymentId AND r.status IN ('COMPLETED', 'PROCESSING')")
    BigDecimal sumRefundedAmountByPaymentId(@Param("paymentId") Long paymentId);
}
