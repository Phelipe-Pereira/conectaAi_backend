package com.conectaai.repository;

import com.conectaai.domain.ApiKey;
import com.conectaai.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByKeyHash(String keyHash);

    List<ApiKey> findByUserAndActiveTrue(User user);

    Page<ApiKey> findByUser(User user, Pageable pageable);

    List<ApiKey> findByUser(User user);

    @Query("SELECT ak FROM ApiKey ak WHERE ak.user = :user AND ak.active = true AND (ak.expiresAt IS NULL OR ak.expiresAt > CURRENT_TIMESTAMP) AND ak.revokedAt IS NULL")
    List<ApiKey> findValidKeysByUser(@Param("user") User user);

    boolean existsByKeyHash(String keyHash);

    @Query("SELECT COUNT(ak) FROM ApiKey ak WHERE ak.user = :user AND ak.active = true")
    long countActiveKeysByUser(@Param("user") User user);

    List<ApiKey> findByKeyPrefix(String keyPrefix);
}

