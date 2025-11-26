package com.conectaai.controller.gateway;

import com.conectaai.dto.gateway.GatewayConfigRequestDto;
import com.conectaai.dto.gateway.GatewayConfigResponseDto;
import com.conectaai.logger.AppLogger;
import com.conectaai.security.SecurityUtils;
import com.conectaai.service.gateway.GatewayConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gateway")
@RequiredArgsConstructor
public class GatewayConfigController {

    private static final AppLogger LOGGER = AppLogger.getLogger(GatewayConfigController.class);
    private final GatewayConfigService gatewayConfigService;

    @PostMapping("/config/asaas")
    public ResponseEntity<Void> saveAsaasApiKey(@Valid @RequestBody GatewayConfigRequestDto request) {
        Long userId = SecurityUtils.getCurrentUserId();
        LOGGER.info("saveAsaasApiKey", "Salvando chave do Asaas para userId: {}", userId);
        
        if (!gatewayConfigService.validateAsaasApiKey(request.asaasApiKey())) {
            return ResponseEntity.badRequest().build();
        }
        
        gatewayConfigService.saveAsaasApiKey(userId, request.asaasApiKey());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/config/asaas")
    public ResponseEntity<GatewayConfigResponseDto> getAsaasConfig() {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean hasKey = gatewayConfigService.hasAsaasApiKey(userId);
        String apiKey = gatewayConfigService.getAsaasApiKey(userId);
        boolean isValid = apiKey != null && gatewayConfigService.validateAsaasApiKey(apiKey);
        
        return ResponseEntity.ok(new GatewayConfigResponseDto(hasKey, isValid));
    }

    @DeleteMapping("/config/asaas")
    public ResponseEntity<Void> removeAsaasApiKey() {
        Long userId = SecurityUtils.getCurrentUserId();
        LOGGER.info("removeAsaasApiKey", "Removendo chave do Asaas para userId: {}", userId);
        gatewayConfigService.removeAsaasApiKey(userId);
        return ResponseEntity.ok().build();
    }
}

