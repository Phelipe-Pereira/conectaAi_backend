package com.conectaai.adapter.factory;

import com.conectaai.adapter.asaas.AsaasWebhookAdapter;
import com.conectaai.adapter.gateway.WebhookGatewayAdapter;
import com.conectaai.adapter.mercadopago.MercadoPagoWebhookAdapter;
import com.conectaai.adapter.stripe.StripeWebhookAdapter;
import com.conectaai.enums.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebhookGatewayAdapterFactory {

    private final AsaasWebhookAdapter asaasAdapter;
    private final StripeWebhookAdapter stripeAdapter;
    private final MercadoPagoWebhookAdapter mercadoPagoAdapter;

    public WebhookGatewayAdapter getAdapter(Provider provider) {
        return switch (provider) {
            case ASAAS -> asaasAdapter;
            case STRIPE -> stripeAdapter;
            case MERCADO_PAGO -> mercadoPagoAdapter;
        };
    }
}

