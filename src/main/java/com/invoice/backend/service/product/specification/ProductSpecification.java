package com.invoice.backend.service.product.specification;

import com.invoice.backend.entity.product.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    public static Specification<Product> hasUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Product> containsTextInNameOrDescription(String text) {
        return (root, query, builder) -> {
            if (text == null || text.isEmpty()) {
                return builder.conjunction();
            }
            String likePattern = "%" + text.toLowerCase() + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("name")), likePattern),
                    builder.like(builder.lower(root.get("description")), likePattern)
            );
        };
    }

    public static Specification<Product> priceGreaterThanOrEqual(Double minPrice) {
        return (root, query, builder) -> {
            if (minPrice == null) {
                return builder.conjunction();
            }
            return builder.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    public static Specification<Product> priceLessThanOrEqual(Double maxPrice) {
        return (root, query, builder) -> {
            if (maxPrice == null) {
                return builder.conjunction();
            }
            return builder.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }
}


