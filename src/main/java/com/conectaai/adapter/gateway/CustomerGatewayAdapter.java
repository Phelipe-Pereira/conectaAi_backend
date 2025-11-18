package com.conectaai.adapter.gateway;

import com.conectaai.domain.Customer;

public interface CustomerGatewayAdapter {

    String createCustomer(Customer customer);

    Object getCustomer(String providerCustomerId);

    void updateCustomer(Customer customer);

    void deleteCustomer(String providerCustomerId);
}

