package com.conectaai.adapter.mercadopago;

import com.conectaai.adapter.gateway.UnsupportedCustomerAdapter;
import com.conectaai.enums.Provider;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoCustomerAdapter extends UnsupportedCustomerAdapter {

    @Override
    protected Provider getProvider() {
        return Provider.MERCADO_PAGO;
    }
}

