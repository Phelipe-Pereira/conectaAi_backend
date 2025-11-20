package com.conectaai.adapter.factory;

import com.conectaai.adapter.asaas.AsaasCustomerAdapter;
import com.conectaai.adapter.gateway.CustomerGatewayAdapter;
import com.conectaai.adapter.mercadopago.MercadoPagoCustomerAdapter;
import com.conectaai.adapter.stripe.StripeCustomerAdapter;
import com.conectaai.enums.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerGatewayAdapterFactory {

    private final AsaasCustomerAdapter asaasAdapter;
    private final StripeCustomerAdapter stripeAdapter;
    private final MercadoPagoCustomerAdapter mercadoPagoAdapter;

    public CustomerGatewayAdapter getAdapter(Provider provider) {
        return switch (provider) {
            case ASAAS -> asaasAdapter;
            case STRIPE -> stripeAdapter;
            case MERCADO_PAGO -> mercadoPagoAdapter;
        };
    }
}

