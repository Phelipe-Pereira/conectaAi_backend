package com.conectaai.repository;

import com.conectaai.domain.Idempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IdempotencyRepository extends JpaRepository<Idempotency, Long> {

    Optional<Idempotency> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<Idempotency> findByIdempotencyKeyAndRequestHash(String idempotencyKey, String requestHash);

    boolean existsByIdempotencyKeyAndRequestHashNot(String idempotencyKey, String requestHash);

    List<Idempotency> findByCreatedAtBefore(LocalDateTime date);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Idempotency> findByResponseBodyIsNull();

    List<Idempotency> findByResponseBodyIsNotNull();

    @Modifying
    @Query("DELETE FROM Idempotency i WHERE i.createdAt < :date")
    void deleteByCreatedAtBefore(@Param("date") LocalDateTime date);

    @Query("SELECT i FROM Idempotency i WHERE i.idempotencyKey = :key ORDER BY i.createdAt ASC")
    List<Idempotency> findAllByIdempotencyKeyOrderByCreatedAt(@Param("key") String idempotencyKey);
}
