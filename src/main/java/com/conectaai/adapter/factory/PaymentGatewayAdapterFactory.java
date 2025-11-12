package com.conectaai.adapter.factory;

import com.conectaai.adapter.asaas.AsaasPaymentAdapter;
import com.conectaai.adapter.gateway.PaymentGatewayAdapter;
import com.conectaai.adapter.mercadopago.MercadoPagoPaymentAdapter;
import com.conectaai.adapter.stripe.StripePaymentAdapter;
import com.conectaai.enums.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentGatewayAdapterFactory {

    private final AsaasPaymentAdapter asaasAdapter;
    private final StripePaymentAdapter stripeAdapter;
    private final MercadoPagoPaymentAdapter mercadoPagoAdapter;

    public PaymentGatewayAdapter getAdapter(Provider provider) {
        return switch (provider) {
            case ASAAS -> asaasAdapter;
            case STRIPE -> stripeAdapter;
            case MERCADO_PAGO -> mercadoPagoAdapter;
        };
    }
}

