package com.conectaai.adapter.stripe;

import com.conectaai.adapter.gateway.UnsupportedCustomerAdapter;
import com.conectaai.enums.Provider;
import org.springframework.stereotype.Component;

@Component
public class StripeCustomerAdapter extends UnsupportedCustomerAdapter {

    @Override
    protected Provider getProvider() {
        return Provider.STRIPE;
    }
}

