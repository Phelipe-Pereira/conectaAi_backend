package com.conectaai.dto.webhook;

import com.conectaai.domain.WebhookEndpoint;
import com.conectaai.domain.User;

import java.util.UUID;

public final class WebhookEndpointMapper {

    private WebhookEndpointMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static WebhookEndpoint toEntity(WebhookEndpointRequestDto request, User user, String providerEndpointId) {
        return WebhookEndpoint.builder()
                .user(user)
                .provider(request.provider())
                .providerEndpointId(providerEndpointId)
                .url(request.url())
                .secret(request.secret() != null ? request.secret() : generateSecret())
                .enabledEvents(request.enabledEvents())
                .active(true)
                .build();
    }

    public static WebhookEndpointResponseDto toResponseDto(WebhookEndpoint endpoint) {
        return new WebhookEndpointResponseDto(
                endpoint.getId(),
                endpoint.getProvider(),
                endpoint.getProviderEndpointId(),
                endpoint.getUrl(),
                endpoint.getActive(),
                endpoint.getEnabledEvents(),
                endpoint.getCreatedAt(),
                endpoint.getUpdatedAt()
        );
    }

    public static void updateEntity(WebhookEndpoint endpoint, WebhookEndpointUpdateDto updateRequest) {
        if (updateRequest.url() != null) {
            endpoint.setUrl(updateRequest.url());
        }
        if (updateRequest.enabledEvents() != null) {
            endpoint.setEnabledEvents(updateRequest.enabledEvents());
        }
        if (updateRequest.active() != null) {
            endpoint.setActive(updateRequest.active());
        }
    }

    private static String generateSecret() {
        return "whsec_" + UUID.randomUUID().toString().replace("-", "");
    }
}
