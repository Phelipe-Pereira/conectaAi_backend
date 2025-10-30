package com.conectaai.controller.subscription;

import com.conectaai.dto.subscription.SubscriptionRequestDto;
import com.conectaai.dto.subscription.SubscriptionResponseDto;
import com.conectaai.dto.subscription.SubscriptionSummaryDto;
import com.conectaai.dto.subscription.SubscriptionUpdateDto;
import com.conectaai.enums.Provider;
import com.conectaai.enums.SubscriptionInterval;
import com.conectaai.enums.SubscriptionStatus;
import com.conectaai.logger.AppLogger;
import com.conectaai.service.subscription.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private static final AppLogger LOGGER = AppLogger.getLogger(SubscriptionController.class);

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "createdAt", "startAt", "endAt", "amount", "status", "interval"
    );

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionResponseDto> createSubscription(@Valid @RequestBody SubscriptionRequestDto subscriptionRequest) {
        LOGGER.info("createSubscription", "Request recebido para criar assinatura");
        SubscriptionResponseDto response = subscriptionService.createSubscription(subscriptionRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<SubscriptionSummaryDto>> findSubscriptions(
            @RequestParam(required = false) String externalId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Provider provider,
            @RequestParam(required = false) SubscriptionStatus status,
            @RequestParam(required = false) SubscriptionInterval interval,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdEnd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        LOGGER.info("findSubscriptions", "Request recebido para listar assinaturas");

        String sanitizedSort = ALLOWED_SORT_FIELDS.contains(sort) ? sort : "createdAt";
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException e) {
            sortDirection = Sort.Direction.DESC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sanitizedSort));

        Page<SubscriptionSummaryDto> subscriptions = subscriptionService.findSubscriptions(
                externalId, customerId, provider, status, interval,
                minAmount, maxAmount, startFrom, startTo, endFrom, endTo,
                createdStart, createdEnd, pageable
        );

        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDto> findById(@PathVariable Long id) {
        LOGGER.info("findById", "Request recebido para buscar assinatura por ID: {}", id);
        SubscriptionResponseDto response = subscriptionService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/external/{externalId}")
    public ResponseEntity<SubscriptionResponseDto> findByExternalId(@PathVariable String externalId) {
        LOGGER.info("findByExternalId", "Request recebido para buscar assinatura por External ID: {}", externalId);
        SubscriptionResponseDto response = subscriptionService.findByExternalId(externalId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<SubscriptionResponseDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionUpdateDto updateRequest
    ) {
        LOGGER.info("updateStatus", "Request recebido para atualizar status da assinatura: {}", id);
        SubscriptionResponseDto response = subscriptionService.updateStatus(id, updateRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelSubscription(@PathVariable Long id) {
        LOGGER.info("cancelSubscription", "Request recebido para cancelar assinatura: {}", id);
        subscriptionService.cancelSubscription(id);
    }

    @PutMapping("/{id}/pause")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pauseSubscription(@PathVariable Long id) {
        LOGGER.info("pauseSubscription", "Request recebido para pausar assinatura: {}", id);
        subscriptionService.pauseSubscription(id);
    }

    @PutMapping("/{id}/resume")
    public ResponseEntity<SubscriptionResponseDto> resumeSubscription(@PathVariable Long id) {
        LOGGER.info("resumeSubscription", "Request recebido para retomar assinatura: {}", id);
        SubscriptionResponseDto response = subscriptionService.resumeSubscription(id);
        return ResponseEntity.ok(response);
    }
}

