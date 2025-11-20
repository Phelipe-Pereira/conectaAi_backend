package com.conectaai.adapter.gateway;

import com.conectaai.domain.Customer;

import java.util.List;

public interface CustomerGatewayAdapter {

    String createCustomer(Customer customer);

    Object getCustomer(String providerCustomerId);

    void updateCustomer(Customer customer);

    void deleteCustomer(String providerCustomerId);

    List<Object> listCustomers(String name, String email, String cpfCnpj, String groupName, String externalReference, Integer offset, Integer limit);

    Object restoreCustomer(String providerCustomerId);
}

