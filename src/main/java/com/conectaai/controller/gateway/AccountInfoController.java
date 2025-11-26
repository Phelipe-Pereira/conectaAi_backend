package com.conectaai.controller.gateway;

import com.conectaai.logger.AppLogger;
import com.conectaai.security.SecurityUtils;
import com.conectaai.service.gateway.AccountInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/gateway/account")
@RequiredArgsConstructor
public class AccountInfoController {

    private static final AppLogger LOGGER = AppLogger.getLogger(AccountInfoController.class);
    private final AccountInfoService accountInfoService;

    @GetMapping("/business-data")
    public ResponseEntity<Map<String, Object>> getBusinessData() {
        Long userId = SecurityUtils.getCurrentUserId();
        LOGGER.info("getBusinessData", "Buscando dados comerciais do Asaas para userId: {}", userId);
        return ResponseEntity.ok(accountInfoService.getBusinessData(userId));
    }

    @GetMapping("/account-number")
    public ResponseEntity<Map<String, Object>> getAccountNumber() {
        Long userId = SecurityUtils.getCurrentUserId();
        LOGGER.info("getAccountNumber", "Buscando número da conta Asaas para userId: {}", userId);
        return ResponseEntity.ok(accountInfoService.getAccountNumber(userId));
    }

    @GetMapping("/fees")
    public ResponseEntity<Map<String, Object>> getAccountFees() {
        Long userId = SecurityUtils.getCurrentUserId();
        LOGGER.info("getAccountFees", "Buscando taxas da conta Asaas para userId: {}", userId);
        return ResponseEntity.ok(accountInfoService.getAccountFees(userId));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAccountStatus() {
        Long userId = SecurityUtils.getCurrentUserId();
        LOGGER.info("getAccountStatus", "Buscando status da conta Asaas para userId: {}", userId);
        return ResponseEntity.ok(accountInfoService.getAccountStatus(userId));
    }

    @GetMapping("/wallet-id")
    public ResponseEntity<Map<String, Object>> getWalletId() {
        Long userId = SecurityUtils.getCurrentUserId();
        LOGGER.info("getWalletId", "Buscando wallet ID do Asaas para userId: {}", userId);
        return ResponseEntity.ok(accountInfoService.getWalletId(userId));
    }
}

