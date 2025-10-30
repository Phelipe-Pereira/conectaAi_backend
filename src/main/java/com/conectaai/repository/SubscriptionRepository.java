package com.conectaai.repository;

import com.conectaai.domain.Subscription;
import com.conectaai.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {

    Optional<Subscription> findByExternalId(String externalId);

    Optional<Subscription> findByProviderAndProviderSubscriptionId(Provider provider, String providerSubscriptionId);

    boolean existsByExternalId(String externalId);
}