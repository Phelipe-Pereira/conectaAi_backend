package com.conectaai.adapter.mercadopago;

import com.conectaai.adapter.gateway.GatewayWebhookResponse;
import com.conectaai.adapter.gateway.WebhookGatewayAdapter;
import com.conectaai.enums.Provider;
import com.conectaai.exception.GatewayException;
import com.conectaai.logger.AppLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MercadoPagoWebhookAdapter implements WebhookGatewayAdapter {

    private static final AppLogger LOGGER = AppLogger.getLogger(MercadoPagoWebhookAdapter.class);
    private static final String MP_WEBHOOKS_DOCS_PATH = "checkout-pro/additional-content/notifications/webhooks";

    @Override
    public GatewayWebhookResponse createWebhook(String url, List<String> events, String secret) {
        LOGGER.info("createWebhook",
                "Mercado Pago webhook configurado via notification_url. " +
                        "URL: {}, Eventos: {}. " +
                        "Esta URL deve ser usada como 'notification_url' ao criar pagamentos/preferências. " +
                        "Webhooks também podem ser configurados no painel: " +
                        "Suas integrações > Webhooks > Configurar notificações",
                url, events);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("configuration_method", "notification_url");
        String mpDocsUrl = "https://www.mercadopago.com.br/developers/pt/docs/" + MP_WEBHOOKS_DOCS_PATH;
        metadata.put("documentation", mpDocsUrl);
        metadata.put("note",
                "URL deve ser configurada via 'notification_url' ao criar pagamentos/preferências, " +
                        "ou via painel 'Suas integrações > Webhooks > Configurar notificações'");

        String endpointId = "mp_wh_" + UUID.randomUUID().toString().replace("-", "");

        return new GatewayWebhookResponse(
                endpointId,
                url,
                secret != null ? secret : "",
                metadata
        );
    }

    @Override
    public GatewayWebhookResponse getWebhook(String providerEndpointId) {
        LOGGER.warn("getWebhook",
                "Mercado Pago não possui API REST para consultar webhooks configurados. " +
                "Consulte no painel: Suas integrações > Webhooks > Painel de notificações. " +
                "ID fornecido: {}", providerEndpointId);

        String mpDocsUrl = "https://www.mercadopago.com.br/developers/pt/docs/" + MP_WEBHOOKS_DOCS_PATH;
        throw new GatewayException(
                Provider.MERCADO_PAGO,
                "Mercado Pago não possui API REST para consultar webhooks. " +
                        "Consulte no painel: Suas integrações > Webhooks > Painel de notificações. " +
                        "Documentação: " + mpDocsUrl
        );
    }

    @Override
    public GatewayWebhookResponse updateWebhook(String providerEndpointId, String url, List<String> events) {
        LOGGER.warn("updateWebhook",
                "Mercado Pago não possui API REST para atualizar webhooks. " +
                "Atualize no painel: Suas integrações > Webhooks > Configurar notificações. " +
                "ID fornecido: {}", providerEndpointId);

        String mpDocsUrl = "https://www.mercadopago.com.br/developers/pt/docs/" + MP_WEBHOOKS_DOCS_PATH;
        throw new GatewayException(
                Provider.MERCADO_PAGO,
                "Mercado Pago não possui API REST para atualizar webhooks. " +
                        "Atualize no painel: Suas integrações > Webhooks > Configurar notificações. " +
                        "Documentação: " + mpDocsUrl
        );
    }

    @Override
    public void deleteWebhook(String providerEndpointId) {
        LOGGER.warn("deleteWebhook",
                "Mercado Pago não possui API REST para deletar webhooks. " +
                "Delete no painel: Suas integrações > Webhooks > Configurar notificações. " +
                "ID fornecido: {}", providerEndpointId);

        String mpDocsUrl = "https://www.mercadopago.com.br/developers/pt/docs/" + MP_WEBHOOKS_DOCS_PATH;
        throw new GatewayException(
                Provider.MERCADO_PAGO,
                "Mercado Pago não possui API REST para deletar webhooks. " +
                        "Delete no painel: Suas integrações > Webhooks > Configurar notificações. " +
                        "Documentação: " + mpDocsUrl
        );
    }
}

