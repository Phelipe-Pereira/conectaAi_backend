package com.conectaai.adapter.factory;

import com.conectaai.adapter.asaas.AsaasCustomerAdapter;
import com.conectaai.adapter.gateway.CustomerGatewayAdapter;
import com.conectaai.enums.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerGatewayAdapterFactory {

    private final AsaasCustomerAdapter asaasAdapter;

    public CustomerGatewayAdapter getAdapter(Provider provider) {
        return switch (provider) {
            case ASAAS -> asaasAdapter;
            case STRIPE -> throw new UnsupportedOperationException("Stripe customer adapter não implementado ainda");
            case MERCADO_PAGO -> throw new UnsupportedOperationException("Mercado Pago customer adapter não implementado ainda");
        };
    }
}

