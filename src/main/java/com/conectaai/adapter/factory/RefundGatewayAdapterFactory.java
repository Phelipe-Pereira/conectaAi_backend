package com.conectaai.adapter.factory;

import com.conectaai.adapter.asaas.AsaasRefundAdapter;
import com.conectaai.adapter.gateway.RefundGatewayAdapter;
import com.conectaai.adapter.mercadopago.MercadoPagoRefundAdapter;
import com.conectaai.adapter.stripe.StripeRefundAdapter;
import com.conectaai.enums.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefundGatewayAdapterFactory {

    private final AsaasRefundAdapter asaasAdapter;
    private final StripeRefundAdapter stripeAdapter;
    private final MercadoPagoRefundAdapter mercadoPagoAdapter;

    public RefundGatewayAdapter getAdapter(Provider provider) {
        return switch (provider) {
            case ASAAS -> asaasAdapter;
            case STRIPE -> stripeAdapter;
            case MERCADO_PAGO -> mercadoPagoAdapter;
        };
    }
}

