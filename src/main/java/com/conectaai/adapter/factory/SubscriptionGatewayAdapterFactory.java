package com.conectaai.adapter.factory;

import com.conectaai.adapter.asaas.AsaasSubscriptionAdapter;
import com.conectaai.adapter.gateway.SubscriptionGatewayAdapter;
import com.conectaai.adapter.mercadopago.MercadoPagoSubscriptionAdapter;
import com.conectaai.adapter.stripe.StripeSubscriptionAdapter;
import com.conectaai.enums.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionGatewayAdapterFactory {

    private final AsaasSubscriptionAdapter asaasAdapter;
    private final StripeSubscriptionAdapter stripeAdapter;
    private final MercadoPagoSubscriptionAdapter mercadoPagoAdapter;

    public SubscriptionGatewayAdapter getAdapter(Provider provider) {
        return switch (provider) {
            case ASAAS -> asaasAdapter;
            case STRIPE -> stripeAdapter;
            case MERCADO_PAGO -> mercadoPagoAdapter;
        };
    }
}

