package com.conectaai.specification;

import com.conectaai.domain.Customer;
import org.springframework.data.jpa.domain.Specification;

public final class CustomerSpecification {

    private CustomerSpecification() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Specification<Customer> hasExternalId(String externalId) {
        return (root, query, criteriaBuilder) -> {
            if (externalId == null || externalId.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("externalId"), externalId);
        };
    }

    public static Specification<Customer> hasActive(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("active"), active);
        };
    }

    public static Specification<Customer> searchByTerm(String term) {
        return (root, query, criteriaBuilder) -> {
            if (term == null || term.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            String likePattern = "%" + term.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern)
            );
        };
    }

    public static Specification<Customer> hasCity(String city) {
        return (root, query, criteriaBuilder) -> {
            if (city == null || city.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("city"), city);
        };
    }

    public static Specification<Customer> hasState(String state) {
        return (root, query, criteriaBuilder) -> {
            if (state == null || state.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("state"), state);
        };
    }

    public static Specification<Customer> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }
}

