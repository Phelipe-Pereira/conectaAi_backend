package com.conectaai.controller.webhook;

import com.conectaai.enums.Provider;
import com.conectaai.logger.AppLogger;
import com.conectaai.service.webhook.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private static final AppLogger LOGGER = AppLogger.getLogger(WebhookController.class);

    private final WebhookService webhookService;

    @PostMapping("/{provider}")
    public ResponseEntity<Void> receive(@PathVariable Provider provider,
                                        @RequestHeader HttpHeaders headers,
                                        @RequestBody String rawPayload) {
        LOGGER.info("receive", "Webhook recebido: provider={}", provider);
        webhookService.handleProviderCallback(provider, headers, rawPayload);
        return ResponseEntity.noContent().build();
    }
}


