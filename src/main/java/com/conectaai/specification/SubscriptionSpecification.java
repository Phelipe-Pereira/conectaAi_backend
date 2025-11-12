package com.conectaai.specification;

import com.conectaai.domain.Subscription;
import com.conectaai.enums.Provider;
import com.conectaai.enums.SubscriptionInterval;
import com.conectaai.enums.SubscriptionStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class SubscriptionSpecification {

    private static final String FIELD_AMOUNT = "amount";
    private static final String FIELD_START_AT = "startAt";
    private static final String FIELD_END_AT = "endAt";
    private static final String FIELD_CREATED_AT = "createdAt";

    private SubscriptionSpecification() {
    }

    public static Specification<Subscription> hasExternalId(String externalId) {
        return (root, query, criteriaBuilder) -> {
            if (externalId == null || externalId.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("externalId"), externalId);
        };
    }

    public static Specification<Subscription> hasCustomerId(Long customerId) {
        return (root, query, criteriaBuilder) -> {
            if (customerId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("customer").get("id"), customerId);
        };
    }

    public static Specification<Subscription> hasProvider(Provider provider) {
        return (root, query, criteriaBuilder) -> {
            if (provider == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("provider"), provider);
        };
    }

    public static Specification<Subscription> hasStatus(SubscriptionStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Subscription> hasInterval(SubscriptionInterval interval) {
        return (root, query, criteriaBuilder) -> {
            if (interval == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("interval"), interval);
        };
    }

    public static Specification<Subscription> amountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return (root, query, criteriaBuilder) -> {
            if (minAmount == null && maxAmount == null) {
                return criteriaBuilder.conjunction();
            }
            if (minAmount != null && maxAmount != null) {
                return criteriaBuilder.between(root.get(FIELD_AMOUNT), minAmount, maxAmount);
            }
            if (minAmount != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_AMOUNT), minAmount);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get(FIELD_AMOUNT), maxAmount);
        };
    }

    public static Specification<Subscription> startBetween(LocalDateTime startFrom, LocalDateTime startTo) {
        return (root, query, criteriaBuilder) -> {
            if (startFrom == null && startTo == null) {
                return criteriaBuilder.conjunction();
            }
            if (startFrom != null && startTo != null) {
                return criteriaBuilder.between(root.get(FIELD_START_AT), startFrom, startTo);
            }
            if (startFrom != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_START_AT), startFrom);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get(FIELD_START_AT), startTo);
        };
    }

    public static Specification<Subscription> endBetween(LocalDateTime endFrom, LocalDateTime endTo) {
        return (root, query, criteriaBuilder) -> {
            if (endFrom == null && endTo == null) {
                return criteriaBuilder.conjunction();
            }
            if (endFrom != null && endTo != null) {
                return criteriaBuilder.between(root.get(FIELD_END_AT), endFrom, endTo);
            }
            if (endFrom != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_END_AT), endFrom);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get(FIELD_END_AT), endTo);
        };
    }

    public static Specification<Subscription> createdBetween(LocalDateTime createdStart, LocalDateTime createdEnd) {
        return (root, query, criteriaBuilder) -> {
            if (createdStart == null && createdEnd == null) {
                return criteriaBuilder.conjunction();
            }
            if (createdStart != null && createdEnd != null) {
                return criteriaBuilder.between(root.get(FIELD_CREATED_AT), createdStart, createdEnd);
            }
            if (createdStart != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_CREATED_AT), createdStart);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get(FIELD_CREATED_AT), createdEnd);
        };
    }
}

