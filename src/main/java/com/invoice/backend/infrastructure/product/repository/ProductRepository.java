package com.invoice.backend.infrastructure.product.repository;

import com.invoice.backend.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Query("SELECT p FROM Product p WHERE p.user.id = :userId AND p.deleted = false")
    List<Product> findActiveByUserId(Long userId);

    @Query("SELECT p FROM Product p WHERE p.user.id = :userId")
    List<Product> findByUserId(Long userId);

    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.user.id = :userId")
    Product findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}