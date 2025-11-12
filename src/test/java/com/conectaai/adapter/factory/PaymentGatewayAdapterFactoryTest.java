package com.conectaai.adapter.factory;

import com.conectaai.adapter.asaas.AsaasPaymentAdapter;
import com.conectaai.adapter.gateway.PaymentGatewayAdapter;
import com.conectaai.adapter.mercadopago.MercadoPagoPaymentAdapter;
import com.conectaai.adapter.stripe.StripePaymentAdapter;
import com.conectaai.enums.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayAdapterFactoryTest {

    @Mock
    private AsaasPaymentAdapter asaasAdapter;

    @Mock
    private StripePaymentAdapter stripeAdapter;

    @Mock
    private MercadoPagoPaymentAdapter mercadoPagoAdapter;

    private PaymentGatewayAdapterFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PaymentGatewayAdapterFactory(asaasAdapter, stripeAdapter, mercadoPagoAdapter);
    }

    @Test
    void getAdapterAsaas() {
        PaymentGatewayAdapter adapter = factory.getAdapter(Provider.ASAAS);
        assertNotNull(adapter);
        assertSame(asaasAdapter, adapter);
    }

    @Test
    void getAdapterStripe() {
        PaymentGatewayAdapter adapter = factory.getAdapter(Provider.STRIPE);
        assertNotNull(adapter);
        assertSame(stripeAdapter, adapter);
    }

    @Test
    void getAdapterMercadoPago() {
        PaymentGatewayAdapter adapter = factory.getAdapter(Provider.MERCADO_PAGO);
        assertNotNull(adapter);
        assertSame(mercadoPagoAdapter, adapter);
    }
}

