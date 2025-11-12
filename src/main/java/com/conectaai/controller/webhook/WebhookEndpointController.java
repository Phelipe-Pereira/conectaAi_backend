package com.conectaai.controller.webhook;

import com.conectaai.config.PaginationConfig;
import com.conectaai.dto.webhook.WebhookEndpointRequestDto;
import com.conectaai.dto.webhook.WebhookEndpointResponseDto;
import com.conectaai.dto.webhook.WebhookEndpointUpdateDto;
import com.conectaai.logger.AppLogger;
import com.conectaai.service.webhook.WebhookEndpointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhook-endpoints")
@RequiredArgsConstructor
public class WebhookEndpointController {

    private static final AppLogger LOGGER = AppLogger.getLogger(WebhookEndpointController.class);

    private final WebhookEndpointService webhookEndpointService;
    private final PaginationConfig paginationConfig;

    @PostMapping
    public ResponseEntity<WebhookEndpointResponseDto> createWebhookEndpoint(
            @AuthenticationPrincipal User principal,
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody WebhookEndpointRequestDto request) {
        LOGGER.info("createWebhookEndpoint", "Criando webhook endpoint para provider: {}", request.provider());
        WebhookEndpointResponseDto response =
                webhookEndpointService.createWebhookEndpoint(authorizationHeader, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<WebhookEndpointResponseDto>> getUserWebhooks(
            @AuthenticationPrincipal User principal,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        LOGGER.info("getUserWebhooks", "Listando webhook endpoints do usuário");
        Pageable pageable = createPageable(page, size, sort, direction);
        Page<WebhookEndpointResponseDto> response =
                webhookEndpointService.getUserWebhooks(authorizationHeader, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WebhookEndpointResponseDto> getWebhookEndpointById(
            @AuthenticationPrincipal User principal,
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long id) {
        LOGGER.info("getWebhookEndpointById", "Buscando webhook endpoint ID: {}", id);
        WebhookEndpointResponseDto response = webhookEndpointService.getWebhookEndpointById(authorizationHeader, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WebhookEndpointResponseDto> updateWebhookEndpoint(
            @AuthenticationPrincipal User principal,
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long id,
            @Valid @RequestBody WebhookEndpointUpdateDto request) {
        LOGGER.info("updateWebhookEndpoint", "Atualizando webhook endpoint ID: {}", id);
        WebhookEndpointResponseDto response =
                webhookEndpointService.updateWebhookEndpoint(authorizationHeader, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebhookEndpoint(
            @AuthenticationPrincipal User principal,
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long id) {
        LOGGER.info("deleteWebhookEndpoint", "Deletando webhook endpoint ID: {}", id);
        webhookEndpointService.deleteWebhookEndpoint(authorizationHeader, id);
        return ResponseEntity.noContent().build();
    }

    private Pageable createPageable(int page, Integer size, String sort, String direction) {
        int pageSize = (size != null && size > 0 && size <= paginationConfig.getMaxPageSize())
                ? size
                : paginationConfig.getDefaultPageSize();

        Sort.Direction sortDirection = parseDirection(direction);
        return PageRequest.of(page, pageSize, Sort.by(sortDirection, sort));
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

