package com.conectaai.dto.subscription;

import com.conectaai.domain.Customer;
import com.conectaai.domain.Subscription;
import com.conectaai.enums.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class SubscriptionMapper {

    private SubscriptionMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Subscription toEntity(SubscriptionRequestDto subscriptionRequest, Customer customer) {
        return Subscription.builder()
                .publicId(UUID.randomUUID().toString())
                .customer(customer)
                .amount(subscriptionRequest.amount())
                .currency(subscriptionRequest.currency())
                .interval(subscriptionRequest.interval())
                .status(SubscriptionStatus.PENDING)
                .startAt(subscriptionRequest.startAt() != null ? subscriptionRequest.startAt() : LocalDateTime.now())
                .endAt(subscriptionRequest.endAt())
                .build();
    }

    public static SubscriptionResponseDto toResponseDto(Subscription subscription) {
        return new SubscriptionResponseDto(
                subscription.getId().toString(),
                subscription.getPublicId(),
                subscription.getCustomer().getId().toString(),
                subscription.getAmount(),
                subscription.getCurrency(),
                subscription.getInterval(),
                subscription.getStatus(),
                subscription.getProvider(),
                subscription.getProviderReference(),
                subscription.getStartAt(),
                subscription.getEndAt(),
                subscription.getCreatedAt()
        );
    }
}

