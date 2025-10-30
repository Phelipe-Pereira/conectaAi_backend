package com.conectaai.specification;

import com.conectaai.domain.Refund;
import com.conectaai.enums.RefundStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class RefundSpecification {

    private RefundSpecification() {
    }

    public static Specification<Refund> hasExternalId(String externalId) {
        return (root, query, criteriaBuilder) ->
                externalId == null ? null : criteriaBuilder.equal(root.get("externalId"), externalId);
    }

    public static Specification<Refund> hasPaymentId(Long paymentId) {
        return (root, query, criteriaBuilder) ->
                paymentId == null ? null : criteriaBuilder.equal(root.get("payment").get("id"), paymentId);
    }

    public static Specification<Refund> hasStatus(RefundStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Refund> amountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (minAmount != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            if (maxAmount != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }
            return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Refund> createdBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (start != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (end != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), end));
            }
            return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Refund> processedBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (start != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("processedAt"), start));
            }
            if (end != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("processedAt"), end));
            }
            return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

