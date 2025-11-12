package com.conectaai.specification;

import com.conectaai.domain.Payment;
import com.conectaai.enums.PaymentStatus;
import com.conectaai.enums.Provider;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class PaymentSpecification {

    private static final String FIELD_AMOUNT = "amount";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_DUE_DATE = "dueDate";
    private static final String FIELD_PAID_AT = "paidAt";

    private PaymentSpecification() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Specification<Payment> hasExternalId(String externalId) {
        return (root, query, criteriaBuilder) -> {
            if (externalId == null || externalId.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("externalId"), externalId);
        };
    }

    public static Specification<Payment> hasCustomerId(Long customerId) {
        return (root, query, criteriaBuilder) -> {
            if (customerId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("customer").get("id"), customerId);
        };
    }

    public static Specification<Payment> hasProvider(Provider provider) {
        return (root, query, criteriaBuilder) -> {
            if (provider == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("provider"), provider);
        };
    }

    public static Specification<Payment> hasStatus(PaymentStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Payment> hasPaymentMethod(String paymentMethod) {
        return (root, query, criteriaBuilder) -> {
            if (paymentMethod == null || paymentMethod.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(criteriaBuilder.upper(root.get("paymentMethod")), paymentMethod.toUpperCase());
        };
    }

    public static Specification<Payment> amountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
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

    public static Specification<Payment> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get(FIELD_CREATED_AT), startDate, endDate);
            }
            if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_CREATED_AT), startDate);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get(FIELD_CREATED_AT), endDate);
        };
    }

    public static Specification<Payment> dueDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get(FIELD_DUE_DATE), startDate, endDate);
            }
            if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_DUE_DATE), startDate);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get(FIELD_DUE_DATE), endDate);
        };
    }

    public static Specification<Payment> paidBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get(FIELD_PAID_AT), startDate, endDate);
            }
            if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_PAID_AT), startDate);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get(FIELD_PAID_AT), endDate);
        };
    }
}

