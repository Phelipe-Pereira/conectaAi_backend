package com.conectaai.repository;

import com.conectaai.domain.User;
import com.conectaai.domain.WebhookEndpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, Long> {

    List<WebhookEndpoint> findByUser(User user);
    List<WebhookEndpoint> findByUserAndActive(User user, Boolean active);
    
    Optional<WebhookEndpoint> findByIdAndUser(Long id, User user);
    
    Page<WebhookEndpoint> findByUser(User user, Pageable pageable);
    Page<WebhookEndpoint> findByUserAndActive(User user, Boolean active, Pageable pageable);
    
    long countByUserAndActive(User user, Boolean active);
    
    boolean existsByUserAndUrl(User user, String url);
}

