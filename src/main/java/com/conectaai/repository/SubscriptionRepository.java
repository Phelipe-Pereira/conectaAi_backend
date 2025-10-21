package com.conectaai.repository;

import com.conectaai.domain.Subscription;
import com.conectaai.enums.Provider;
import com.conectaai.enums.SubscriptionInterval;
import com.conectaai.enums.SubscriptionStatus;
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
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByPublicId(String publicId);

    List<Subscription> findByCustomerId(Long customerId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    List<Subscription> findByProvider(Provider provider);

    List<Subscription> findByInterval(SubscriptionInterval interval);

    List<Subscription> findByEndAtBefore(LocalDateTime date);
    List<Subscription> findByEndAtAfter(LocalDateTime date);
    List<Subscription> findByEndAtBetween(LocalDateTime start, LocalDateTime end);
    List<Subscription> findByStartAtBefore(LocalDateTime date);
    List<Subscription> findByStartAtAfter(LocalDateTime date);
    List<Subscription> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Subscription> findByCustomerIdAndStatus(Long customerId, SubscriptionStatus status);
    List<Subscription> findByProviderAndStatus(Provider provider, SubscriptionStatus status);
    List<Subscription> findByStatusAndInterval(SubscriptionStatus status, SubscriptionInterval interval);

    boolean existsByPublicId(String publicId);
    boolean existsByCustomerIdAndStatus(Long customerId, SubscriptionStatus status);
    boolean existsByProviderReference(String providerReference);

    long countByStatus(SubscriptionStatus status);
    long countByCustomerId(Long customerId);
    long countByProvider(Provider provider);
    long countByInterval(SubscriptionInterval interval);

    List<Subscription> findByAmount(BigDecimal amount);
    List<Subscription> findByAmountGreaterThan(BigDecimal amount);
    List<Subscription> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    Optional<Subscription> findByProviderReference(String providerReference);

    Page<Subscription> findByCustomerId(Long customerId, Pageable pageable);
    Page<Subscription> findByStatus(SubscriptionStatus status, Pageable pageable);
    Page<Subscription> findByProvider(Provider provider, Pageable pageable);

    @Query("SELECT s FROM Subscription s WHERE s.status = com.conectaai.enums.SubscriptionStatus.ACTIVE "
            + "AND s.endAt BETWEEN :now AND :futureDate")
    List<Subscription> findActiveSubscriptionsExpiringBetween(
            @Param("now") LocalDateTime now,
            @Param("futureDate") LocalDateTime futureDate);

    @Query("SELECT SUM(s.amount) FROM Subscription s WHERE s.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.customer.id = :customerId "
            + "AND s.status = com.conectaai.enums.SubscriptionStatus.ACTIVE")
    List<Subscription> findActiveSubscriptionsByCustomerId(@Param("customerId") Long customerId);
}