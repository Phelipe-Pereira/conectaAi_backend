package com.conectaai.repository;

import com.conectaai.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByExternalId(String externalId);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByCpf(String cpf);
    Optional<Customer> findByCnpj(String cnpj);

    boolean existsByEmail(String email);
    boolean existsByExternalId(String externalId);
    boolean existsByCpf(String cpf);
    boolean existsByCnpj(String cnpj);
}
