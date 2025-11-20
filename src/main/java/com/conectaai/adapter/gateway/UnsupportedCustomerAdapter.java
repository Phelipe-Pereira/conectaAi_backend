package com.conectaai.adapter.gateway;

import com.conectaai.domain.Customer;
import com.conectaai.enums.Provider;

import java.util.List;

public abstract class UnsupportedCustomerAdapter implements CustomerGatewayAdapter {

    protected abstract Provider getProvider();

    private String getErrorMessage(String operation) {
        return String.format("Funcionalidade de %s não está disponível para %s", operation, getProvider().name());
    }

    @Override
    public String createCustomer(Customer customer) {
        throw new IllegalArgumentException(getErrorMessage("criação de customer"));
    }

    @Override
    public Object getCustomer(String providerCustomerId) {
        throw new IllegalArgumentException(getErrorMessage("busca de customer"));
    }

    @Override
    public void updateCustomer(Customer customer) {
        throw new IllegalArgumentException(getErrorMessage("atualização de customer"));
    }

    @Override
    public void deleteCustomer(String providerCustomerId) {
        throw new IllegalArgumentException(getErrorMessage("exclusão de customer"));
    }

    @Override
    public List<Object> listCustomers(String name, String email, String cpfCnpj, String groupName, String externalReference, Integer offset, Integer limit) {
        throw new IllegalArgumentException(getErrorMessage("listagem de customers"));
    }

    @Override
    public Object restoreCustomer(String providerCustomerId) {
        throw new IllegalArgumentException(getErrorMessage("restauração de customer"));
    }
}

