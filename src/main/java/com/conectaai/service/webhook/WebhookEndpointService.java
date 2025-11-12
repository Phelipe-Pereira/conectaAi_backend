package com.conectaai.service.webhook;

import com.conectaai.adapter.factory.WebhookGatewayAdapterFactory;
import com.conectaai.adapter.gateway.GatewayWebhookResponse;
import com.conectaai.adapter.gateway.WebhookGatewayAdapter;
import com.conectaai.domain.User;
import com.conectaai.domain.WebhookEndpoint;
import com.conectaai.dto.webhook.WebhookEndpointMapper;
import com.conectaai.dto.webhook.WebhookEndpointRequestDto;
import com.conectaai.dto.webhook.WebhookEndpointResponseDto;
import com.conectaai.dto.webhook.WebhookEndpointUpdateDto;
import com.conectaai.exception.GatewayException;
import com.conectaai.exception.WebhookEndpointException;
import com.conectaai.logger.AppLogger;
import com.conectaai.repository.WebhookEndpointRepository;
import com.conectaai.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebhookEndpointService {

    private static final AppLogger LOGGER = AppLogger.getLogger(WebhookEndpointService.class);
    private static final String METHOD_CREATE_WEBHOOK_ENDPOINT = "createWebhookEndpoint";
    private static final String METHOD_UPDATE_WEBHOOK_ENDPOINT = "updateWebhookEndpoint";
    private static final String METHOD_DELETE_WEBHOOK_ENDPOINT = "deleteWebhookEndpoint";
    private static final String ERROR_WEBHOOK_ENDPOINT_NOT_FOUND = "Webhook endpoint não encontrado: ";

    private final WebhookEndpointRepository webhookEndpointRepository;
    private final WebhookGatewayAdapterFactory adapterFactory;
    private final AuthService authService;

    @Transactional
    public WebhookEndpointResponseDto createWebhookEndpoint(String userToken, WebhookEndpointRequestDto request) {
        LOGGER.info(METHOD_CREATE_WEBHOOK_ENDPOINT, "Criando webhook endpoint: provider={}, url={}", 
                    request.provider(), request.url());

        User user = authService.getUserFromToken(userToken);

        if (webhookEndpointRepository.existsByUserAndUrl(user, request.url())) {
            throw new IllegalArgumentException("Webhook endpoint já existe com esta URL: " + request.url());
        }

        WebhookGatewayAdapter adapter = adapterFactory.getAdapter(request.provider());

        try {
            GatewayWebhookResponse gatewayResponse = adapter.createWebhook(
                    request.url(),
                    request.enabledEvents(),
                    request.secret()
            );

            WebhookEndpoint endpoint = WebhookEndpointMapper.toEntity(
                    request,
                    user,
                    gatewayResponse.providerEndpointId()
            );

            if (request.secret() == null || request.secret().isBlank()) {
                String gatewaySecret = gatewayResponse.secret();
                if (gatewaySecret != null && !gatewaySecret.isBlank()) {
                    endpoint.setSecret(gatewaySecret);
                }
            }

            if (request.provider() == com.conectaai.enums.Provider.MERCADO_PAGO) {
                LOGGER.info(METHOD_CREATE_WEBHOOK_ENDPOINT,
                        "Mercado Pago: Webhook configurado localmente. " +
                        "URL deve ser usada como 'notification_url' ao criar pagamentos/preferências. " +
                        "Ou configure via painel: Suas integrações > Webhooks > Configurar notificações");
            }

            WebhookEndpoint savedEndpoint = webhookEndpointRepository.save(endpoint);

            LOGGER.info(METHOD_CREATE_WEBHOOK_ENDPOINT, "Webhook endpoint criado: id={}, providerEndpointId={}", 
                        savedEndpoint.getId(), savedEndpoint.getProviderEndpointId());

            return WebhookEndpointMapper.toResponseDto(savedEndpoint);

        } catch (GatewayException e) {
            LOGGER.error(METHOD_CREATE_WEBHOOK_ENDPOINT, "Erro do gateway ao criar webhook", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error(METHOD_CREATE_WEBHOOK_ENDPOINT, "Erro inesperado ao criar webhook endpoint", e);
            throw new WebhookEndpointException("Erro ao criar webhook endpoint: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Page<WebhookEndpointResponseDto> getUserWebhooks(String userToken, Pageable pageable) {
        User user = authService.getUserFromToken(userToken);
        return webhookEndpointRepository.findByUser(user, pageable)
                .map(WebhookEndpointMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public WebhookEndpointResponseDto getWebhookEndpointById(String userToken, Long id) {
        User user = authService.getUserFromToken(userToken);
        WebhookEndpoint endpoint = webhookEndpointRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Webhook endpoint não encontrado: " + id));
        return WebhookEndpointMapper.toResponseDto(endpoint);
    }

    @Transactional
    public WebhookEndpointResponseDto updateWebhookEndpoint(
            String userToken, Long id, WebhookEndpointUpdateDto updateRequest) {
        LOGGER.info(METHOD_UPDATE_WEBHOOK_ENDPOINT, "Atualizando webhook endpoint: id={}", id);

        User user = authService.getUserFromToken(userToken);
        String errorMsg = "Webhook endpoint não encontrado: " + id;
        WebhookEndpoint endpoint = webhookEndpointRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException(errorMsg));

        WebhookGatewayAdapter adapter = adapterFactory.getAdapter(endpoint.getProvider());

        try {
            if (endpoint.getProviderEndpointId() != null) {
                adapter.updateWebhook(
                        endpoint.getProviderEndpointId(),
                        updateRequest.url(),
                        updateRequest.enabledEvents()
                );
            }

            WebhookEndpointMapper.updateEntity(endpoint, updateRequest);
            WebhookEndpoint updatedEndpoint = webhookEndpointRepository.save(endpoint);

            LOGGER.info(METHOD_UPDATE_WEBHOOK_ENDPOINT, "Webhook endpoint atualizado: id={}", updatedEndpoint.getId());

            return WebhookEndpointMapper.toResponseDto(updatedEndpoint);

        } catch (GatewayException e) {
            LOGGER.error(METHOD_UPDATE_WEBHOOK_ENDPOINT, "Erro do gateway ao atualizar webhook", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error(METHOD_UPDATE_WEBHOOK_ENDPOINT, "Erro inesperado ao atualizar webhook endpoint", e);
            throw new WebhookEndpointException("Erro ao atualizar webhook endpoint: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteWebhookEndpoint(String userToken, Long id) {
        LOGGER.info(METHOD_DELETE_WEBHOOK_ENDPOINT, "Deletando webhook endpoint: id={}", id);

        User user = authService.getUserFromToken(userToken);
        WebhookEndpoint endpoint = webhookEndpointRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Webhook endpoint não encontrado: " + id));

        WebhookGatewayAdapter adapter = adapterFactory.getAdapter(endpoint.getProvider());

        try {
            if (endpoint.getProviderEndpointId() != null) {
                adapter.deleteWebhook(endpoint.getProviderEndpointId());
            }

            webhookEndpointRepository.delete(endpoint);

            LOGGER.info(METHOD_DELETE_WEBHOOK_ENDPOINT, "Webhook endpoint deletado: id={}", id);

        } catch (GatewayException e) {
            LOGGER.error(METHOD_DELETE_WEBHOOK_ENDPOINT, "Erro do gateway ao deletar webhook", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error(METHOD_DELETE_WEBHOOK_ENDPOINT, "Erro inesperado ao deletar webhook endpoint", e);
            throw new WebhookEndpointException("Erro ao deletar webhook endpoint: " + e.getMessage(), e);
        }
    }
}

