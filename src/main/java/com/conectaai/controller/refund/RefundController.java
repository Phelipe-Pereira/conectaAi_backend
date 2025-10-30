package com.conectaai.controller.refund;

import com.conectaai.dto.refund.RefundRequestDto;
import com.conectaai.dto.refund.RefundResponseDto;
import com.conectaai.dto.refund.RefundSummaryDto;
import com.conectaai.dto.refund.RefundUpdateDto;
import com.conectaai.enums.RefundStatus;
import com.conectaai.logger.AppLogger;
import com.conectaai.service.refund.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
public class RefundController {

    private static final AppLogger LOGGER = AppLogger.getLogger(RefundController.class);

    private final RefundService refundService;

    @PostMapping
    public ResponseEntity<RefundResponseDto> createRefund(@Valid @RequestBody RefundRequestDto refundRequest) {
        LOGGER.info("createRefund", "Request recebido para criar reembolso");
        RefundResponseDto response = refundService.createRefund(refundRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<RefundSummaryDto>> findRefunds(
            @RequestParam(required = false) String externalId,
            @RequestParam(required = false) Long paymentId,
            @RequestParam(required = false) RefundStatus status,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) LocalDateTime createdStart,
            @RequestParam(required = false) LocalDateTime createdEnd,
            @RequestParam(required = false) LocalDateTime processedStart,
            @RequestParam(required = false) LocalDateTime processedEnd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection
    ) {
        LOGGER.info("findRefunds", "Request recebido para listar reembolsos com filtros");
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<RefundSummaryDto> refunds = refundService.findRefunds(
                externalId, paymentId, status,
                minAmount, maxAmount,
                createdStart, createdEnd,
                processedStart, processedEnd,
                pageable
        );
        return ResponseEntity.ok(refunds);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefundResponseDto> findById(@PathVariable Long id) {
        LOGGER.info("findById", "Request recebido para buscar reembolso por ID: {}", id);
        RefundResponseDto refund = refundService.findById(id);
        return ResponseEntity.ok(refund);
    }

    @GetMapping("/external/{externalId}")
    public ResponseEntity<RefundResponseDto> findByExternalId(@PathVariable String externalId) {
        LOGGER.info("findByExternalId", "Request recebido para buscar reembolso por External ID: {}", externalId);
        RefundResponseDto refund = refundService.findByExternalId(externalId);
        return ResponseEntity.ok(refund);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RefundResponseDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody RefundUpdateDto updateRequest
    ) {
        LOGGER.info("updateStatus", "Request recebido para atualizar status do reembolso ID: {}", id);
        RefundResponseDto updatedRefund = refundService.updateStatus(id, updateRequest);
        return ResponseEntity.ok(updatedRefund);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelRefund(@PathVariable Long id) {
        LOGGER.info("cancelRefund", "Request recebido para cancelar reembolso ID: {}", id);
        refundService.cancelRefund(id);
    }
}

