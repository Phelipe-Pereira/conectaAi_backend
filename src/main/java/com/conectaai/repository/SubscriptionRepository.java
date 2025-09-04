package com.conectaai.repository;

import com.conectaai.domain.Subscription;
import com.conectaai.enums.Provider;
import com.conectaai.enums.SubscriptionInterval;
import com.conectaai.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
    List<Subscription> findByEndAtBetween(LocalDateTime start, LocalDateTime end);
    List<Subscription> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Subscription> findByCustomerIdAndStatus(Long customerId, SubscriptionStatus status);
    List<Subscription> findByProviderAndStatus(Provider provider, SubscriptionStatus status);

    boolean existsByPublicId(String publicId);
    boolean existsByCustomerIdAndStatus(Long customerId, SubscriptionStatus status);

    long countByStatus(SubscriptionStatus status);
    long countByCustomerId(Long customerId);
}