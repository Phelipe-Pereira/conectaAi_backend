package com.conectaai.specification;

import com.conectaai.domain.Payment;
import com.conectaai.enums.PaymentStatus;
import com.conectaai.enums.Provider;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class PaymentSpecification {

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
                return criteriaBuilder.between(root.get("amount"), minAmount, maxAmount);
            }
            if (minAmount != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount);
        };
    }

    public static Specification<Payment> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("createdAt"), startDate, endDate);
            }
            if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
        };
    }

    public static Specification<Payment> dueDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("dueDate"), startDate, endDate);
            }
            if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), startDate);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), endDate);
        };
    }

    public static Specification<Payment> paidBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("paidAt"), startDate, endDate);
            }
            if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("paidAt"), startDate);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("paidAt"), endDate);
        };
    }
}

