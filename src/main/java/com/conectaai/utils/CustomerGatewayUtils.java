package com.conectaai.utils;

import com.conectaai.domain.Customer;
import com.conectaai.enums.Provider;
import com.conectaai.logger.AppLogger;

public final class CustomerGatewayUtils {

    private static final AppLogger LOGGER = AppLogger.getLogger(CustomerGatewayUtils.class);

    private CustomerGatewayUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String getCustomerIdForGateway(Customer customer) {
        if (customer.getProviderCustomerId() != null && !customer.getProviderCustomerId().isBlank()) {
            return customer.getProviderCustomerId();
        }
        return customer.getExternalId();
    }

    public static boolean needsGatewayCreation(Customer customer, Provider provider) {
        return provider == Provider.ASAAS && 
               (customer.getProviderCustomerId() == null || customer.getProviderCustomerId().isBlank());
    }
}

