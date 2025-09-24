package com.conectaai.repository;

import com.conectaai.domain.Idempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyRepository extends JpaRepository<Idempotency, Long> {

}
