package com.conectaai.dto.subscription;

import com.conectaai.domain.Customer;
import com.conectaai.domain.Subscription;
import com.conectaai.dto.customer.CustomerSummaryDto;
import com.conectaai.enums.SubscriptionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public final class SubscriptionMapper {

    private SubscriptionMapper() {
    }

    public static Subscription toEntity(SubscriptionRequestDto subscriptionRequest, Customer customer) {
        return Subscription.builder()
                .externalId(generateExternalId())
                .customer(customer)
                .provider(subscriptionRequest.provider())
                .amount(subscriptionRequest.amount())
                .currency(subscriptionRequest.currency())
                .interval(subscriptionRequest.interval())
                .paymentMethod(subscriptionRequest.paymentMethod())
                .description(subscriptionRequest.description())
                .startAt(subscriptionRequest.startAt() != null ? subscriptionRequest.startAt().atStartOfDay() : null)
                .endAt(subscriptionRequest.endAt() != null ? subscriptionRequest.endAt().atStartOfDay() : null)
                .status(SubscriptionStatus.PENDING)
                .build();
    }

    public static SubscriptionResponseDto toResponseDto(Subscription subscription) {
        return new SubscriptionResponseDto(
                subscription.getId(),
                subscription.getExternalId(),
                subscription.getProviderSubscriptionId(),
                subscription.getProvider(),
                toCustomerSummary(subscription.getCustomer()),
                subscription.getAmount(),
                subscription.getCurrency(),
                subscription.getInterval(),
                subscription.getStatus(),
                subscription.getPaymentMethod(),
                subscription.getDescription(),
                subscription.getStartAt() != null ? subscription.getStartAt().toLocalDate() : null,
                subscription.getEndAt() != null ? subscription.getEndAt().toLocalDate() : null,
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }

    public static SubscriptionSummaryDto toSummaryDto(Subscription subscription) {
        return new SubscriptionSummaryDto(
                subscription.getId(),
                subscription.getExternalId(),
                subscription.getProvider(),
                toCustomerSummary(subscription.getCustomer()),
                subscription.getAmount(),
                subscription.getCurrency(),
                subscription.getInterval(),
                subscription.getStatus(),
                subscription.getPaymentMethod(),
                subscription.getStartAt() != null ? subscription.getStartAt().toLocalDate() : null,
                subscription.getEndAt() != null ? subscription.getEndAt().toLocalDate() : null,
                subscription.getCreatedAt()
        );
    }

    public static void updateEntity(Subscription subscription, SubscriptionUpdateDto updateRequest) {
        if (updateRequest.status() != null) {
            subscription.setStatus(updateRequest.status());
        }
        if (updateRequest.providerSubscriptionId() != null) {
            subscription.setProviderSubscriptionId(updateRequest.providerSubscriptionId());
        }
        if (updateRequest.endAt() != null) {
            subscription.setEndAt(updateRequest.endAt().atStartOfDay());
        }
        if (updateRequest.description() != null) {
            subscription.setDescription(updateRequest.description());
        }
    }

    private static CustomerSummaryDto toCustomerSummary(Customer customer) {
        if (customer == null) {
            return null;
        }
        return new CustomerSummaryDto(
                customer.getId(),
                customer.getExternalId(),
                customer.getFullName(),
                customer.getEmail()
        );
    }

    private static String generateExternalId() {
        return "sub_" + UUID.randomUUID().toString().replace("-", "");
    }
}
