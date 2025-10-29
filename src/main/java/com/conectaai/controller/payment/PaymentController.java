package com.conectaai.controller.payment;

import com.conectaai.config.PaginationConfig;
import com.conectaai.dto.payment.PaymentRequestDto;
import com.conectaai.dto.payment.PaymentResponseDto;
import com.conectaai.dto.payment.PaymentSummaryDto;
import com.conectaai.dto.payment.PaymentUpdateDto;
import com.conectaai.enums.PaymentStatus;
import com.conectaai.enums.Provider;
import com.conectaai.logger.AppLogger;
import com.conectaai.service.payment.PaymentService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private static final AppLogger LOGGER = AppLogger.getLogger(PaymentController.class);
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "createdAt", "amount", "dueDate", "status");

    private final PaymentService paymentService;
    private final PaginationConfig paginationConfig;

    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(@Valid @RequestBody PaymentRequestDto paymentRequest) {
        LOGGER.info("createPayment", "Requisição recebida para criar pagamento");
        PaymentResponseDto response = paymentService.createPayment(paymentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<PaymentSummaryDto>> findPayments(
            @RequestParam(required = false) String externalId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Provider provider,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) LocalDateTime createdStart,
            @RequestParam(required = false) LocalDateTime createdEnd,
            @RequestParam(required = false) LocalDate dueStart,
            @RequestParam(required = false) LocalDate dueEnd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        LOGGER.info("findPayments", "Requisição recebida para listar pagamentos");

        Pageable pageable = createPageable(page, size, sort, direction);

        Page<PaymentSummaryDto> payments = paymentService.findPayments(
                externalId, customerId, provider, status, paymentMethod,
                minAmount, maxAmount, createdStart, createdEnd, dueStart, dueEnd, pageable
        );

        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable Long id) {
        LOGGER.info("getPaymentById", "Requisição recebida para buscar pagamento ID: {}", id);
        PaymentResponseDto response = paymentService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/external/{externalId}")
    public ResponseEntity<PaymentResponseDto> getPaymentByExternalId(@PathVariable String externalId) {
        LOGGER.info("getPaymentByExternalId", "Requisição recebida para buscar pagamento ExternalID: {}", externalId);
        PaymentResponseDto response = paymentService.findByExternalId(externalId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentResponseDto> updatePaymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody PaymentUpdateDto updateRequest
    ) {
        LOGGER.info("updatePaymentStatus", "Requisição recebida para atualizar status do pagamento ID: {}", id);
        PaymentResponseDto response = paymentService.updateStatus(id, updateRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelPayment(@PathVariable Long id) {
        LOGGER.info("cancelPayment", "Requisição recebida para cancelar pagamento ID: {}", id);
        paymentService.cancelPayment(id);
        return ResponseEntity.noContent().build();
    }

    private Pageable createPageable(int page, Integer size, String sort, String direction) {
        int pageSize = (size != null && size > 0 && size <= paginationConfig.getMaxPageSize())
                ? size
                : paginationConfig.getDefaultPageSize();

        Sort.Direction sortDirection = parseDirection(direction);

        String validatedSortField = ALLOWED_SORT_FIELDS.contains(sort) ? sort : "createdAt";

        return PageRequest.of(page, pageSize, Sort.by(sortDirection, validatedSortField));
    }

    private Sort.Direction parseDirection(String direction) {
        try {
            return Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("parseDirection", "Direção de ordenação inválida: {}. Usando DESC como padrão", direction);
            return Sort.Direction.DESC;
        }
    }
}

