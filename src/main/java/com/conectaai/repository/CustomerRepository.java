package com.conectaai.repository;

import com.conectaai.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByExternalId(String externalId);
    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByExternalId(String externalId);
    boolean existsByDocument(String document);

    List<Customer> findByNameContainingIgnoreCase(String name);
    List<Customer> findByEmailContainingIgnoreCase(String email);

    List<Customer> findByCreatedAtAfter(LocalDateTime date);
    List<Customer> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByCreatedAtAfter(LocalDateTime date);

    Page<Customer> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Customer> findAll(Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:searchTerm% OR c.email LIKE %:searchTerm%")
    List<Customer> findByNameOrEmailContaining(@Param("searchTerm") String searchTerm);

    @Query("SELECT c FROM Customer c WHERE c.document IS NOT NULL")
    List<Customer> findCustomersWithDocument();

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt >= :startDate AND c.createdAt <= :endDate")
    long countCustomersCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}