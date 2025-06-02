package com.invoice.backend.service.client.specification;

import com.invoice.backend.entity.client.Client;
import org.springframework.data.jpa.domain.Specification;

public class ClientSpecification {

    public static Specification<Client> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isEmpty()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Client> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isEmpty()) return null;
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public static Specification<Client> hasPhoneNumber(String phoneNumber) {
        return (root, query, cb) -> {
            if (phoneNumber == null || phoneNumber.isEmpty()) return null;
            return cb.like(root.get("phoneNumber"), "%" + phoneNumber + "%");
        };
    }
}
