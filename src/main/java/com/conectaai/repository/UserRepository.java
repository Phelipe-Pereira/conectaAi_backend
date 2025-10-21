package com.conectaai.repository;

import com.conectaai.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    List<User> findByActive(Boolean active);

    List<User> findByCreatedAtAfter(LocalDateTime date);
    List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<User> findByUpdatedAtAfter(LocalDateTime date);
    List<User> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByActive(Boolean active);
    long countByCreatedAtAfter(LocalDateTime date);

    Page<User> findByActive(Boolean active, Pageable pageable);
    Page<User> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);
}