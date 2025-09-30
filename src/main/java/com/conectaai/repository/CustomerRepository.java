package com.conectaai.repository;

import com.conectaai.domain.Customer;
import com.conectaai.enums.DocumentType;
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
    Optional<Customer> findByDocument(String document);

    boolean existsByEmail(String email);
    boolean existsByExternalId(String externalId);
    boolean existsByDocument(String document);

    List<Customer> findByFirstNameContainingIgnoreCase(String firstName);
    List<Customer> findByLastNameContainingIgnoreCase(String lastName);
    List<Customer> findByEmailContainingIgnoreCase(String email);

    List<Customer> findByCreatedAtAfter(LocalDateTime date);
    List<Customer> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByCreatedAtAfter(LocalDateTime date);

    List<Customer> findByActiveTrue();
    List<Customer> findByActiveFalse();

    List<Customer> findByDocumentType(DocumentType documentType);
    List<Customer> findByCity(String city);
    List<Customer> findByState(String state);
    List<Customer> findByCountry(String country);

    Page<Customer> findByActiveTrue(Pageable pageable);

    @Query("""
      SELECT c
      FROM Customer c
      WHERE LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :term, '%'))
         OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :term, '%'))
         OR LOWER(c.lastName)  LIKE LOWER(CONCAT('%', :term, '%'))
         OR LOWER(c.email)     LIKE LOWER(CONCAT('%', :term, '%'))
    """)
    Page<Customer> searchByNameOrEmail(@Param("term") String term, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.document IS NOT NULL")
    List<Customer> findCustomersWithDocument();

    @Query("""
      SELECT COUNT(c)
      FROM Customer c
      WHERE c.createdAt >= :startDate AND c.createdAt <= :endDate
    """)
    long countCustomersCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    @Query("""
      SELECT c FROM Customer c
      WHERE c.active = true
        AND LOWER(c.email) LIKE LOWER(CONCAT('%@', :domain))
    """)
    List<Customer> findActiveByEmailDomain(@Param("domain") String domain);

    @Query("""
      SELECT c FROM Customer c
      WHERE c.document IS NOT NULL
        AND c.documentType = :type
        AND c.active = true
    """)
    List<Customer> findActiveByDocumentType(@Param("type") DocumentType type);
}
